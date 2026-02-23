const asyncHandler = require("express-async-handler")
const MusicPlayed = require("../models/musicPlayedModel")

// @desc    Get total plays by listener
// @route   GET /api/stats/listener/:userId
// @access  Private (Listener)
const getListenerStats = asyncHandler(async (req, res) => {
    const userId = req.params.userId
    // Count all play records for this user
    const totalPlays = await MusicPlayed.countDocuments({ userId })
    res.json({ totalPlays })
})

module.exports = {
    getListenerStats
}
