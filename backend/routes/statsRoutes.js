const express = require("express")
const router = express.Router()
const { getArtistStats, getAdminStats } = require("../controllers/statsController")
const { protect, artist, admin } = require("../middleware/authMiddleware")

router.get("/artist", protect, artist, getArtistStats)
router.get("/admin", protect, admin, getAdminStats)

module.exports = router
