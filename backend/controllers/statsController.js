const asyncHandler = require("express-async-handler")
const Music = require("../models/musicModel")
const User = require("../models/userModel")
const Follow = require("../models/followModel")

// @desc    Get artist dashboard stats
// @route   GET /api/stats/artist
// @access  Private (Artist)
const getArtistStats = asyncHandler(async (req, res) => {
    const artistId = req.user._id

    // Total plays across all songs
    const music = await Music.find({ artist: artistId })
    const totalPlays = music.reduce((acc, curr) => acc + curr.plays, 0)

    // Total followers
    const followersCount = await Follow.countDocuments({ followingId: artistId })

    // Music data for charts (last 7 days?)
    // This would require a separate "Plays" model to track daily plays.
    // For now, returning top songs
    const topSongs = await Music.find({ artist: artistId })
        .sort({ plays: -1 })
        .limit(5)

    res.json({
        totalPlays,
        followersCount,
        totalSongs: music.length,
        topSongs
    })
})

// @desc    Get admin dashboard stats
// @route   GET /api/stats/admin
// @access  Private (Admin)
const getAdminStats = asyncHandler(async (req, res) => {
    const totalUsers = await User.countDocuments()
    const totalArtists = await User.countDocuments({ userType: "ARTIST" })
    const totalMusic = await Music.countDocuments()
    const pendingApproval = await Music.countDocuments({ isApproved: false })

    // Recent activity
    const recentMusic = await Music.find().sort({ createdAt: -1 }).limit(5)

    res.json({
        totalUsers,
        totalArtists,
        totalMusic,
        pendingApproval,
        recentMusic
    })
})

module.exports = {
    getArtistStats,
    getAdminStats
}
