const asyncHandler = require("express-async-handler")
const Music = require("../models/musicModel")
const User = require("../models/userModel")
const Follow = require("../models/followModel")

// @desc    Search music and artists
// @route   GET /api/search
// @access  Public
const searchAll = asyncHandler(async (req, res) => {
    const query = req.query.q

    if (!query) {
        return res.status(400).json({ message: "Search query is required" })
    }

    const regex = new RegExp(query, "i")

    // Search for music
    const music = await Music.find({
        isApproved: true,
        $or: [
            { title: regex },
            { genre: regex },
            { album: regex },
            { artistName: regex }
        ]
    }).limit(20).lean()

    // Search for artists
    let artists = await User.find({
        userType: "ARTIST",
        $or: [
            { name: regex },
            { fullName: regex },
            { bio: regex }
        ]
    }).select("-password").limit(10).lean()

    // Attach follower counts to artists
    artists = await Promise.all(
        artists.map(async (artist) => {
            const followerCount = await Follow.countDocuments({ followingId: artist._id })
            return { ...artist, followerCount }
        })
    )

    res.json({ music, artists })
})

module.exports = {
    searchAll
}
