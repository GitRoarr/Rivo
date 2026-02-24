const express = require("express")
const router = express.Router()
const { getArtistStats, getAdminStats } = require("../controllers/statsController")
const { getListenerStats } = require("../controllers/listenerStatsController")
const { protect, artist, admin } = require("../middleware/authMiddleware")

router.get("/artist", protect, artist, getArtistStats)
router.get("/admin", protect, admin, getAdminStats)
router.get("/listener/:userId", protect, getListenerStats)

module.exports = router
