const asyncHandler = require("express-async-handler")
const Music = require("../models/musicModel")
const User = require("../models/userModel")
const Follow = require("../models/followModel")
const FeaturedContent = require("../models/featuredContentModel")


const getExploreData = asyncHandler(async (req, res) => {
    const trendingMusic = await Music.find({ isApproved: true })
        .sort({ plays: -1 })
        .limit(10)

    const newReleases = await Music.find({ isApproved: true })
        .sort({ createdAt: -1 })
        .limit(10)

    const artists = await User.find({ userType: "ARTIST" })
        .select("-password")
        .limit(10)

    const featuredArtists = await Promise.all(
        artists.map(async (artist) => {
            const followerCount = await Follow.countDocuments({ followingId: artist._id })
            return { ...artist.toObject(), followerCount }
        })
    )
    // Featured Music: Random selection of 5 approved songs
    const featuredMusic = await Music.aggregate([
        { $match: { isApproved: true } },
        { $sample: { size: 5 } }
    ])

    // Fetch banners from FeaturedContent
    const featuredBanners = await FeaturedContent.find({ type: "BANNER", isActive: true }).sort({ order: 1 })

    // Map to frontend format (especially description -> subtitle)
    let dynamicBanners = featuredBanners.map(banner => ({
        id: banner.id || banner._id,
        imageUrl: banner.imageUrl,
        title: banner.title,
        subtitle: banner.description || banner.subtitle || "",
        actionUrl: banner.actionUrl
    }))

    // Fallback if no dynamic banners exist
    if (dynamicBanners.length === 0) {
        dynamicBanners = [
            {
                id: "1",
                imageUrl: "https://res.cloudinary.com/do2guqnvl/image/upload/v1708365000/rivo/banners/banner1.jpg",
                title: "Trending Music",
                subtitle: "Listen to the most popular tracks right now",
                actionUrl: "/trending"
            },
            {
                id: "2",
                imageUrl: "https://res.cloudinary.com/do2guqnvl/image/upload/v1708365000/rivo/banners/banner2.jpg",
                title: "New Releases",
                subtitle: "Fresh sounds from your favorite artists",
                actionUrl: "/new-releases"
            }
        ]
    }

    res.json({
        trendingMusic,
        newReleases,
        featuredArtists,
        featuredMusic,
        banners: dynamicBanners,
        categories: [
            { id: "1", title: "Pop", color: "#FF6B6B", icon: "music_note" },
            { id: "2", title: "Hip-Hop", color: "#4ECDC4", icon: "mic" },
            { id: "3", title: "Electronic", color: "#45B7D1", icon: "album" },
            { id: "4", title: "Rock", color: "#FFA07A", icon: "electric_guitar" },
            { id: "5", title: "Jazz", color: "#98D8C8", icon: "trumpet" },
            { id: "6", title: "Chill", color: "#F7D794", icon: "spa" }
        ]
    })
})

module.exports = {
    getExploreData
}
