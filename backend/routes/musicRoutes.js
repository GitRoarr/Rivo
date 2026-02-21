const express = require("express")
const router = express.Router()
const {
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
} = require("../controllers/musicController")
const { protect, artist, admin } = require("../middleware/authMiddleware")
const { upload } = require("../config/cloudinary")

// Admin-only routes (must come before /:id to avoid conflict)
router.get("/admin/all", protect, admin, getAllMusicAdmin)
router.get("/pending", protect, admin, getPendingMusic)

router.route("/")
    .get(getAllMusic)
    .post(protect, artist, upload.fields([
        { name: 'audio', maxCount: 1 },
        { name: 'coverImage', maxCount: 1 }
    ]), uploadMusic)

router.get("/artist/:artistId", getMusicByArtist)

router.route("/:id")
    .get(getMusicById)
    .put(protect, artist, upload.single('coverImage'), updateMusic)
    .delete(protect, artist, deleteMusic)

router.post("/:id/play", incrementPlays)
router.put("/:id/approve", protect, admin, approveMusicAdmin)
router.put("/:id/reject", protect, admin, rejectMusicAdmin)

module.exports = router
