const asyncHandler = require("express-async-handler")
const Music = require("../models/musicModel")
const User = require("../models/userModel")
const Follow = require("../models/followModel")
const Notification = require("../models/notificationModel")
const MusicPlayed = require("../models/musicPlayedModel")

// @desc    Get artist dashboard stats
// @route   GET /api/stats/artist
// @access  Private (Artist)
const getArtistStats = asyncHandler(async (req, res) => {
    const artistId = req.user._id

    // Total songs by this artist
    const music = await Music.find({ artist: artistId })
    const totalPlays = music.reduce((acc, curr) => acc + (curr.plays || 0), 0)

    const musicIds = music.map((m) => m._id)

    // Total followers
    const followersCount = await Follow.countDocuments({ followingId: artistId.toString() })

    // Total following
    const followingCount = await Follow.countDocuments({ followerId: artistId.toString() })

    // Monthly listeners (unique listeners in the last 30 days across all artist tracks)
    let monthlyListeners = 0
    if (musicIds.length > 0) {
        const thirtyDaysAgoForListeners = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
        const monthlyListenersAgg = await MusicPlayed.aggregate([
            {
                $match: {
                    musicId: { $in: musicIds },
                    playedAt: { $gte: thirtyDaysAgoForListeners },
                },
            },
            { $group: { _id: "$userId" } },
            { $count: "count" },
        ])

        monthlyListeners = monthlyListenersAgg[0]?.count || 0
    }

    // Top performing songs
    const topSongs = await Music.find({ artist: artistId })
        .sort({ plays: -1 })
        .limit(5)

    // Recent uploads (last 30 days)
    const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    const recentUploads = await Music.find({
        artist: artistId,
        createdAt: { $gte: thirtyDaysAgo }
    }).sort({ createdAt: -1 })

    // Pending approval count
    const pendingCount = await Music.countDocuments({ artist: artistId, isApproved: false })

    // Unread notifications
    const unreadNotifications = await Notification.countDocuments({ user: artistId, isRead: false })

    res.json({
        totalPlays,
        followersCount,
        followingCount,
        monthlyListeners,
        totalSongs: music.length,
        topSongs,
        recentUploads,
        pendingCount,
        unreadNotifications,
    })
})

// @desc    Get admin dashboard stats
// @route   GET /api/stats/admin
// @access  Private (Admin)
const getAdminStats = asyncHandler(async (req, res) => {
    const totalUsers = await User.countDocuments()
    const totalArtists = await User.countDocuments({ userType: "ARTIST" })
    const totalListeners = await User.countDocuments({ userType: "LISTENER" })
    const totalMusic = await Music.countDocuments()
    const pendingApproval = await Music.countDocuments({ isApproved: false })

    // Total plays across platform
    const musicDocs = await Music.find()
    const totalPlays = musicDocs.reduce((acc, curr) => acc + (curr.plays || 0), 0)

    // Recent music
    const recentMusic = await Music.find().sort({ createdAt: -1 }).limit(10)

    // Recent users
    const recentUsers = await User.find().sort({ createdAt: -1 }).limit(5).select("-password")

    // Pending verifications
    const pendingVerifications = await User.countDocuments({
        userType: "ARTIST",
        verificationStatus: "PENDING"
    })

    // New users today
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const newUsersToday = await User.countDocuments({ createdAt: { $gte: today } })

    // New music today
    const newMusicToday = await Music.countDocuments({ createdAt: { $gte: today } })

    res.json({
        totalUsers,
        totalArtists,
        totalListeners,
        totalMusic,
        totalPlays,
        pendingApproval,
        pendingVerifications,
        recentMusic,
        recentUsers,
        newUsersToday,
        newMusicToday
    })
})

module.exports = {
    getArtistStats,
    getAdminStats
}
