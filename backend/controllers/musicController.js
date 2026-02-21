const asyncHandler = require("express-async-handler")
const Music = require("../models/musicModel")
const User = require("../models/userModel")

// @desc    Get all approved music (public) or all music (admin)
// @route   GET /api/music
// @access  Public
const getAllMusic = asyncHandler(async (req, res) => {
    const { genre, search, artist } = req.query
    const query = { isApproved: true }

    if (genre) query.genre = genre
    if (artist) query.artist = artist
    if (search) {
        query.$or = [
            { title: { $regex: search, $options: "i" } },
            { album: { $regex: search, $options: "i" } }
        ]
    }

    const music = await Music.find(query).sort({ createdAt: -1 })
    res.json(music)
})

// @desc    Get ALL music including unapproved (admin only)
// @route   GET /api/music/admin/all
// @access  Private (Admin)
const getAllMusicAdmin = asyncHandler(async (req, res) => {
    const music = await Music.find({}).sort({ createdAt: -1 })
    res.json(music)
})

// @desc    Get music by ID
// @route   GET /api/music/:id
// @access  Public
const getMusicById = asyncHandler(async (req, res) => {
    const music = await Music.findById(req.params.id)

    if (music) {
        res.json(music)
    } else {
        res.status(404)
        throw new Error("Music not found")
    }
})

// @desc    Upload music
// @route   POST /api/music
// @access  Private (Artist/Admin)
const uploadMusic = asyncHandler(async (req, res) => {
    const { title, genre, album, duration } = req.body

    if (!title || !genre) {
        res.status(400)
        throw new Error("Please provide title and genre")
    }

    // Get URLs from uploaded files
    console.log("Uploaded files:", req.files);

    const audioUrl = req.files && req.files.audio ? req.files.audio[0].path : null
    const coverImageUrl = req.files && req.files.coverImage ? req.files.coverImage[0].path : ""

    if (!audioUrl) {
        res.status(400)
        throw new Error("Please upload an audio file")
    }

    const music = await Music.create({
        title,
        artist: req.user._id,
        artistName: req.user.name,
        genre,
        album,
        url: audioUrl,
        coverImageUrl,
        duration,
        isApproved: req.user.userType === "ADMIN" // Admins' uploads are auto-approved
    })

    if (music) {
        res.status(201).json(music)
    } else {
        res.status(400)
        throw new Error("Invalid music data")
    }
})

// @desc    Update music
// @route   PUT /api/music/:id
// @access  Private (Artist)
const updateMusic = asyncHandler(async (req, res) => {
    const music = await Music.findById(req.params.id)

    if (!music) {
        res.status(404)
        throw new Error("Music not found")
    }

    // Check if user is the artist or admin
    if (music.artist.toString() !== req.user._id.toString() && req.user.userType !== "ADMIN") {
        res.status(401)
        throw new Error("Not authorized to update this music")
    }

    const updates = { ...req.body }

    // Handle cover image update
    if (req.file) {
        updates.coverImageUrl = req.file.path
    }

    const updatedMusic = await Music.findByIdAndUpdate(req.params.id, updates, {
        new: true,
    })

    res.json(updatedMusic)
})

// @desc    Delete music
// @route   DELETE /api/music/:id
// @access  Private (Artist/Admin)
const deleteMusic = asyncHandler(async (req, res) => {
    const music = await Music.findById(req.params.id)

    if (!music) {
        res.status(404)
        throw new Error("Music not found")
    }

    // Check if user is the artist or admin
    if (music.artist.toString() !== req.user._id.toString() && req.user.userType !== "ADMIN") {
        res.status(401)
        throw new Error("Not authorized to delete this music")
    }

    await music.deleteOne()
    res.json({ message: "Music removed" })
})

// @desc    Get songs by artist
// @route   GET /api/music/artist/:artistId
// @access  Public
const getMusicByArtist = asyncHandler(async (req, res) => {
    const music = await Music.find({ artist: req.params.artistId })
    res.json(music)
})

// @desc    Increment play count
// @route   POST /api/music/:id/play
// @access  Public
const incrementPlays = asyncHandler(async (req, res) => {
    const music = await Music.findById(req.params.id)

    if (music) {
        music.plays += 1
        await music.save()
        res.json({ plays: music.plays })
    } else {
        res.status(404)
        throw new Error("Music not found")
    }
})

// @desc    Approve music (Admin only)
// @route   PUT /api/music/:id/approve
// @access  Private (Admin)
const approveMusicAdmin = asyncHandler(async (req, res) => {
    const music = await Music.findById(req.params.id)

    if (!music) {
        res.status(404)
        throw new Error("Music not found")
    }

    music.isApproved = true
    await music.save()

    res.json({ message: "Music approved", music })
})

// @desc    Reject music (Admin only)
// @route   PUT /api/music/:id/reject
// @access  Private (Admin)
const rejectMusicAdmin = asyncHandler(async (req, res) => {
    const music = await Music.findById(req.params.id)

    if (!music) {
        res.status(404)
        throw new Error("Music not found")
    }

    music.isApproved = false
    await music.save()

    res.json({ message: "Music rejected", music })
})

// @desc    Get pending (unapproved) music
// @route   GET /api/music/pending
// @access  Private (Admin)
const getPendingMusic = asyncHandler(async (req, res) => {
    const pendingMusic = await Music.find({ isApproved: false }).sort({ createdAt: -1 })
    res.json(pendingMusic)
})

module.exports = {
    getAllMusic,
    getAllMusicAdmin,
    getMusicById,
    uploadMusic,
    updateMusic,
    deleteMusic,
    getMusicByArtist,
    incrementPlays,
    approveMusicAdmin,
    rejectMusicAdmin,
    getPendingMusic
}
