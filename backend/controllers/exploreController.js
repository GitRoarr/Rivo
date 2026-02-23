const mongoose = require("mongoose")
const asyncHandler = require("express-async-handler")
const Music = require("../models/musicModel")
const User = require("../models/userModel")
const Follow = require("../models/followModel")
const FeaturedContent = require("../models/featuredContentModel")
const Category = require("../models/categoryModel")

/**
 * @desc    Get all data for the Explore/Home screen
 * @route   GET /api/explore
 * @access  Public
 */
const getExploreData = asyncHandler(async (req, res) => {
    // 1. Trending Music: Mostly based on play counts
    const trendingMusic = await Music.find({ isApproved: true })
        .sort({ plays: -1 })
        .limit(10)

    // 2. New Releases: Sorted by creation date
    const newReleases = await Music.find({ isApproved: true })
        .sort({ createdAt: -1 })
        .limit(10)

    // 3. Featured Artists: Prioritize pinned artists from FeaturedContent, fallback to most followed
    const featuredArtistEntries = await FeaturedContent.find({ type: "ARTIST", isActive: true }).sort({ order: 1 })

    let featuredArtists = []
    if (featuredArtistEntries.length > 0) {
        const artistIds = featuredArtistEntries.map(entry => entry.contentId).filter(id => id)
        const pinnedArtists = await User.find({ _id: { $in: artistIds } }).select("-password")

        // Mantain the order from FeaturedContent
        featuredArtists = artistIds.map(id => pinnedArtists.find(a => a._id.toString() === id.toString())).filter(a => a)
    }

    // Fallback/Fill up to 10 artists if needed
    if (featuredArtists.length < 10) {
        const existingIds = featuredArtists.map(a => a._id.toString())
        const otherArtists = await User.find({
            userType: "ARTIST",
            _id: { $nin: existingIds }
        })
            .select("-password")
            .limit(10 - featuredArtists.length)

        featuredArtists = [...featuredArtists, ...otherArtists]
    }

    // Attach follower counts to artists
    featuredArtists = await Promise.all(
        featuredArtists.map(async (artist) => {
            const followerCount = await Follow.countDocuments({ followingId: artist._id })
            return { ...artist.toObject(), followerCount }
        })
    )

    // 4. Featured Music / Recommendations: Prioritize pinned songs from FeaturedContent
    const featuredSongEntries = await FeaturedContent.find({ type: "SONG", isActive: true }).sort({ order: 1 })

    let featuredMusic = []
    if (featuredSongEntries.length > 0) {
        const songIds = featuredSongEntries.map(entry => entry.contentId).filter(id => id)
        featuredMusic = await Music.find({ _id: { $in: songIds }, isApproved: true })
    }

    // Fallback/Sample if no pinned songs OR less than 5
    if (featuredMusic.length < 5) {
        const existingIds = featuredMusic.map(m => m._id.toString())
        const randomSample = await Music.aggregate([
            { $match: { isApproved: true, _id: { $nin: existingIds.map(id => mongoose.Types.ObjectId(id)) } } },
            { $sample: { size: 5 - featuredMusic.length } }
        ])
        featuredMusic = [...featuredMusic, ...randomSample]
    }

    // 5. Banners: Purely admin driven with logical fallbacks
    const featuredBanners = await FeaturedContent.find({ type: "BANNER", isActive: true }).sort({ order: 1 })

    let dynamicBanners = featuredBanners.map(banner => ({
        id: banner.id || banner._id,
        imageUrl: banner.imageUrl,
        title: banner.title,
        subtitle: banner.description || "",
        actionUrl: banner.actionUrl
    }))

    // Logical fallback if no banners exist - use real content artwork to look "amazing"
    if (dynamicBanners.length === 0) {
        // Find best quality artwork among trending/new releases
        const trendingArt = trendingMusic.find(m => m.artworkUri)?.artworkUri
            || "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=2070&auto=format&fit=crop"

        const newArt = newReleases.find(m => m.artworkUri)?.artworkUri
            || "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=2070&auto=format&fit=crop"

        dynamicBanners = [
            {
                id: "default-trending",
                imageUrl: trendingArt,
                title: "Trending Music",
                subtitle: "Listen to the most popular tracks currently on Rivo",
                actionUrl: "/music/trending"
            },
            {
                id: "default-new",
                imageUrl: newArt,
                title: "New Releases",
                subtitle: "Explore the latest sounds from global artists",
                actionUrl: "/music/new"
            }
        ]
    }

    // 6. Categories: Curated genres
    let categories = await Category.find().sort({ createdAt: 1 })

    // Seed logic if no categories exist (Self-healing backend)
    if (categories.length === 0) {
        const defaultCategories = [
            { title: "Pop", color: "#FF3B6B", icon: "music_note" },
            { title: "Hip-Hop", color: "#4E6DC4", icon: "mic" },
            { title: "Electronic", color: "#45B7D1", icon: "album" },
            { title: "Afrobeat", color: "#FFD700", icon: "music_note" },
            { title: "Acoustic", color: "#98D8C8", icon: "music_note" },
            { title: "Chill", color: "#A0A0A0", icon: "spa" }
        ]
        categories = await Category.create(defaultCategories)
    }

    res.json({
        trendingMusic,
        newReleases,
        featuredArtists,
        featuredMusic,
        banners: dynamicBanners,
        categories: categories.map(cat => ({
            id: cat._id || cat.id,
            title: cat.title,
            color: cat.color,
            icon: cat.icon
        }))
    })
})

module.exports = {
    getExploreData
}
