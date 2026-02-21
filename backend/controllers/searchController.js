const asyncHandler = require("express-async-handler")
const Music = require("../models/musicModel")
const User = require("../models/userModel")

// @desc    Search music and artists
// @route   GET /api/search
// @access  Public
const searchAll = asyncHandler(async (req, res) => {
    const query = req.query.q

    if (!query) {
        return res.status(400).json({ message: "Search query is required" })
    }

    const music = await Music.find({
        isApproved: true,
        $or: [
            { title: { $regex: query, $options: "i" } },
            { genre: { $regex: query, $options: "i" } },
            { album: { $regex: query, $options: "i" } }
        ]
    }).limit(10)

    const artists = await User.find({
        userType: "ARTIST",
        $or: [
            { name: { $regex: query, $options: "i" } },
            { fullName: { $regex: query, $options: "i" } }
        ]
    }).select("-password").limit(10)

    res.json({ music, artists })
})

module.exports = {
    searchAll
}
