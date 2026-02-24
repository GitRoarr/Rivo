const asyncHandler = require("express-async-handler")
const jwt = require("jsonwebtoken")
const bcrypt = require("bcryptjs")
const User = require("../models/userModel")
const VerificationRequest = require("../models/verificationRequestModel")

const generateToken = (id) => {
    return jwt.sign({ id }, process.env.JWT_SECRET, {
        expiresIn: "30d",
    })
}


const registerUser = asyncHandler(async (req, res) => {
    const { id, email, password, name, fullName, userType } = req.body

    console.log("Register request received:", { id, email, name, fullName, userType })

    // Ensure all required fields are provided
    if (!email || !password || !name || !fullName || !userType) {
        res.status(400)
        throw new Error("Please provide all required fields")
    }

    // Check if the user already exists
    const userExists = await User.findOne({ email })
    if (userExists) {
        res.status(400)
        throw new Error("User already exists")
    }

    // Create a new user
    const user = await User.create({
        _id: id, // Use the provided ID
        email,
        password, // Model will hash this via pre-save hook
        name,
        fullName,
        userType,
        verificationStatus: "UNVERIFIED",
    })

    if (user) {
        console.log("User created successfully:", user.email)
        res.status(201).json({
            id: user._id,
            email: user.email,
            name: user.name,
            fullName: user.fullName,
            userType: user.userType,
            verificationStatus: user.verificationStatus,
            token: generateToken(user._id),
        })
    } else {
        res.status(400)
        throw new Error("Invalid user data")
    }
})


const loginUser = asyncHandler(async (req, res) => {
    const { email, password } = req.body

    console.log("Login request received:", { email })

    // Check for user email
    const user = await User.findOne({ email })

    if (user && (await user.matchPassword(password))) {
        console.log("Login successful for:", user.email)
        res.json({
            id: user._id,
            email: user.email,
            name: user.name,
            fullName: user.fullName,
            userType: user.userType,
            verificationStatus: user.verificationStatus,
            token: generateToken(user._id),
        })
    } else {
        console.log("Login failed for:", email)
        res.status(401)
        throw new Error("Invalid credentials")
    }
})


const getUserProfile = asyncHandler(async (req, res) => {
    const user = await User.findById(req.user._id).select("-password")

    if (user) {
        res.json(user)
    } else {
        res.status(404)
        throw new Error("User not found")
    }
})

const updateUserProfile = asyncHandler(async (req, res) => {
    const user = await User.findById(req.user._id)

    if (user) {
        console.log("Profile update request for:", user._id)
        if (req.files) {
            console.log("Files received:", req.files)
        }
        console.log("Body received:", req.body)

        user.fullName = req.body.fullName || user.fullName
        user.bio = req.body.bio !== undefined ? req.body.bio : user.bio
        user.location = req.body.location || user.location
        user.website = req.body.website || user.website

        // Handle profile image upload
        if (req.files && req.files.profileImage) {
            user.profileImageUrl = req.files.profileImage[0].path
            console.log("Updated profile image URL:", user.profileImageUrl)
        } else if (req.body.profileImageUrl) {
            user.profileImageUrl = req.body.profileImageUrl
        }

        // Handle cover image upload
        if (req.files && req.files.coverImage) {
            user.coverImageUrl = req.files.coverImage[0].path
            console.log("Updated cover image URL:", user.coverImageUrl)
        } else if (req.body.coverImageUrl) {
            user.coverImageUrl = req.body.coverImageUrl
        }

        if (req.body.password) {
            user.password = req.body.password // Will be hashed by pre-save hook
        }

        const updatedUser = await user.save()
        console.log("User updated successfully:", updatedUser._id)

        res.json({
            id: updatedUser._id,
            email: updatedUser.email,
            name: updatedUser.name,
            fullName: updatedUser.fullName,
            userType: updatedUser.userType,
            bio: updatedUser.bio,
            profileImageUrl: updatedUser.profileImageUrl,
            coverImageUrl: updatedUser.coverImageUrl,
            location: updatedUser.location,
            website: updatedUser.website,
            verificationStatus: updatedUser.verificationStatus,
            isApproved: updatedUser.isApproved,
            isSuspended: updatedUser.isSuspended,
            createdAt: updatedUser.createdAt
        })
    } else {
        res.status(404)
        throw new Error("User not found")
    }
})


const getUserById = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id).select("-password -likedSongs")

    if (user) {
        res.json(user)
    } else {
        res.status(404)
        throw new Error("User not found")
    }
})


const getUserByEmail = asyncHandler(async (req, res) => {
    const user = await User.findOne({ email: req.params.email }).select("-password")

    if (user) {
        res.json(user)
    } else {
        res.status(404)
        throw new Error("User not found")
    }
})

const checkUserExists = asyncHandler(async (req, res) => {
    const user = await User.findOne({ email: req.params.email })
    res.json({ exists: !!user })
})


const deleteUser = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id)

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    if (req.user._id.toString() !== user._id.toString() && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to delete this user")
    }

    await user.deleteOne()

    res.json({ message: "User removed" })
})


const updateUserType = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id)

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    if (req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to update user type")
    }

    user.userType = req.query.type
    const updatedUser = await user.save()

    res.json(updatedUser)
})

const submitVerificationRequest = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id)

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    if (req.user._id.toString() !== user._id.toString() && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized")
    }

    if (user.userType !== "ARTIST") {
        res.status(400)
        throw new Error("Only artists can request verification")
    }

    // Capture the additional form inputs and uploaded files
    const { artistName, email, phoneNumber, location, primaryGenre, artistBio, socialLinks } = req.body;
    let idDocumentUrl = '';
    let proofOfArtistryUrl = '';

    if (req.files && req.files.idDocument) {
        idDocumentUrl = req.files.idDocument[0].path;
    }
    if (req.files && req.files.proofOfArtistry) {
        proofOfArtistryUrl = req.files.proofOfArtistry[0].path;
    }

    if (!idDocumentUrl || !proofOfArtistryUrl) {
        res.status(400);
        throw new Error("ID Document and Proof of Artistry are required.");
    }

    let parsedSocialLinks = {};
    if (socialLinks) {
        try {
            parsedSocialLinks = typeof socialLinks === 'string' ? JSON.parse(socialLinks) : socialLinks;
        } catch (e) {
            console.error("Could not parse social links.");
        }
    }

    // Check if an existing pending request is there
    let verificationRequest = await VerificationRequest.findOne({ userId: user._id, status: 'PENDING' });

    if (verificationRequest) {
        // Update existing pending request
        verificationRequest.artistName = artistName;
        verificationRequest.email = email;
        verificationRequest.phoneNumber = phoneNumber;
        verificationRequest.location = location;
        verificationRequest.primaryGenre = primaryGenre;
        verificationRequest.artistBio = artistBio;
        verificationRequest.socialLinks = parsedSocialLinks;
        verificationRequest.idDocumentUrl = idDocumentUrl;
        verificationRequest.proofOfArtistryUrl = proofOfArtistryUrl;
        await verificationRequest.save();
    } else {
        // Create new request
        verificationRequest = await VerificationRequest.create({
            userId: user._id,
            artistName,
            email,
            phoneNumber,
            location,
            primaryGenre,
            artistBio,
            socialLinks: parsedSocialLinks,
            idDocumentUrl,
            proofOfArtistryUrl
        });
    }

    user.verificationStatus = "PENDING"
    await user.save()

    res.status(201).json({ message: "Verification request submitted successfully." })
})


const getVerificationStatus = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id)

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    if (req.user._id.toString() !== user._id.toString() && req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized")
    }

    // Optionally fetch the actual request document
    const verificationRequest = await VerificationRequest.findOne({ userId: user._id }).sort({ createdAt: -1 });

    res.json({ status: user.verificationStatus, request: verificationRequest })
})

const updateVerificationStatus = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id)

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    if (req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to update verification status")
    }

    user.verificationStatus = req.body.status
    await user.save()

    res.json({ message: "Verification status updated" })
})

const resetPassword = asyncHandler(async (req, res) => {
    const { email, password } = req.query

    const user = await User.findOne({ email })

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    user.password = password
    await user.save()

    res.json({ message: "Password reset successful" })
})

const approveArtist = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id)

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    if (req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to approve artists")
    }

    user.isApproved = req.query.approved === "true"
    await user.save()

    res.json({ message: `Artist ${user.isApproved ? "approved" : "unapproved"} successfully` })
})


const suspendUser = asyncHandler(async (req, res) => {
    const user = await User.findById(req.params.id)

    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    if (req.user.userType !== "ADMIN") {
        res.status(403)
        throw new Error("Not authorized to suspend users")
    }

    user.isSuspended = req.query.suspended === "true"
    await user.save()

    res.json({ message: `User ${user.isSuspended ? "suspended" : "unsuspended"} successfully` })
})


const Follow = require("../models/followModel")

const followUser = asyncHandler(async (req, res) => {
    const followingId = req.params.id
    const followerId = req.user._id

    // Prevent users from following themselves
    if (followingId === followerId.toString()) {
        res.status(400)
        throw new Error("You cannot follow yourself")
    }

    const follow = await Follow.create({
        followerId,
        followingId,
    })

    res.status(201).json({ message: "Followed successfully" })
})

const unfollowUser = asyncHandler(async (req, res) => {
    const followingId = req.params.id
    const followerId = req.user._id

    await Follow.findOneAndDelete({ followerId, followingId })

    res.json({ message: "Unfollowed successfully" })
})

const getFollowers = asyncHandler(async (req, res) => {
    const follows = await Follow.find({ followingId: req.params.id }).populate("followerId", "name email profileImageUrl")
    const followers = follows.map(f => f.followerId)
    res.json(followers)
})

const getFollowing = asyncHandler(async (req, res) => {
    const follows = await Follow.find({ followerId: req.params.id }).populate("followingId", "name email profileImageUrl")
    const following = follows.map(f => f.followingId)
    res.json(following)
})

// @desc    Get all users (Admin only)
// @route   GET /api/users
// @access  Private/Admin
const getAllUsers = asyncHandler(async (req, res) => {
    const users = await User.find({}).select("-password")
    res.json(users)
})

// @desc    Get users awaiting verification (Admin only)
// @route   GET /api/users/verification/pending
// @access  Private/Admin
const getUsersAwaitingVerification = asyncHandler(async (req, res) => {
    const users = await User.find({ verificationStatus: "PENDING" }).select("-password")
    res.json(users)
})

const Music = require("../models/musicModel")

// @desc    Like a song (add to likedSongs)
// @route   POST /api/users/liked-songs/:musicId
// @access  Private
const likeMusic = asyncHandler(async (req, res) => {
    const user = await User.findById(req.user._id)
    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    const musicId = req.params.musicId
    if (!user.likedSongs.includes(musicId)) {
        user.likedSongs.push(musicId)
        await user.save()
    }

    res.json({ liked: true, musicId })
})

// @desc    Unlike a song (remove from likedSongs)
// @route   DELETE /api/users/liked-songs/:musicId
// @access  Private
const unlikeMusic = asyncHandler(async (req, res) => {
    const user = await User.findById(req.user._id)
    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    const musicId = req.params.musicId
    user.likedSongs = user.likedSongs.filter(id => id.toString() !== musicId)
    await user.save()

    res.json({ liked: false, musicId })
})

// @desc    Get all liked songs for the current user
// @route   GET /api/users/liked-songs
// @access  Private
const getLikedSongs = asyncHandler(async (req, res) => {
    const user = await User.findById(req.user._id)
    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    const songs = await Music.find({ _id: { $in: user.likedSongs } })
    res.json(songs)
})

// @desc    Check if a song is liked by the current user
// @route   GET /api/users/liked-songs/check/:musicId
// @access  Private
const checkLikedSong = asyncHandler(async (req, res) => {
    const user = await User.findById(req.user._id)
    if (!user) {
        res.status(404)
        throw new Error("User not found")
    }

    const liked = user.likedSongs.includes(req.params.musicId)
    res.json({ liked, musicId: req.params.musicId })
})

module.exports = {
    registerUser,
    loginUser,
    getUserProfile,
    updateUserProfile,
    getUserById,
    getUserByEmail,
    checkUserExists,
    deleteUser,
    updateUserType,
    submitVerificationRequest,
    getVerificationStatus,
    updateVerificationStatus,
    resetPassword,
    approveArtist,
    suspendUser,
    followUser,
    unfollowUser,
    getFollowers,
    getFollowing,
    getAllUsers,
    getUsersAwaitingVerification,
    likeMusic,
    unlikeMusic,
    getLikedSongs,
    checkLikedSong
}