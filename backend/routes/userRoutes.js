const express = require("express")
const router = express.Router()
const {
    registerUser,
    loginUser,
    getUserProfile,
    updateUserProfile,
    getUserById,
    getUserByEmail,
    checkUserExists,
    deleteUser,
    updateUserType,
    submitVerificationRequest,
    getVerificationStatus,
    updateVerificationStatus,
    resetPassword,
    approveArtist,
    suspendUser,
    followUser,
    unfollowUser,
    getFollowers,
    getFollowing,
    checkFollowStatus,
    getAllUsers,
    getUsersAwaitingVerification,
    likeMusic,
    unlikeMusic,
    getLikedSongs,
    checkLikedSong,
} = require("../controllers/userController")
const { protect, admin } = require("../middleware/authMiddleware")
const { upload } = require("../config/cloudinary")

// ── Public routes ────────────────────────────────────────────────────────────
router.post("/register", registerUser)
router.post("/login", loginUser)
router.post("/reset-password", resetPassword)

// ── Specific named routes (must come before /:id wildcard) ───────────────────
router.get("/exists/:email", checkUserExists)
router.get("/email/:email", protect, getUserByEmail)
router.get("/profile", protect, getUserProfile)
router.put("/profile", protect, upload.fields([
    { name: 'profileImage', maxCount: 1 },
    { name: 'coverImage', maxCount: 1 }
]), updateUserProfile)

// ── Liked songs routes (must come before /:id wildcard) ─────────────────────
router.get("/liked-songs", protect, getLikedSongs)
router.get("/liked-songs/check/:musicId", protect, checkLikedSong)
router.post("/liked-songs/:musicId", protect, likeMusic)
router.delete("/liked-songs/:musicId", protect, unlikeMusic)

// Admin-only management (must come BEFORE /:id wildcard)
router.get("/", protect, admin, getAllUsers)
router.get("/verification/pending", protect, admin, getUsersAwaitingVerification)

// ── Routes parameterised by userId (:id) ─────────────────────────────────────
router.get("/:id", protect, getUserById)
router.delete("/:id", protect, deleteUser)

// Follow / unfollow
router.post("/:id/follow", protect, followUser)
router.delete("/:id/follow", protect, unfollowUser)
router.get("/:id/followers", protect, getFollowers)
router.get("/:id/following", protect, getFollowing)
router.get("/:id/is-following", protect, checkFollowStatus)

// Verification (artist submits, admin reviews)
router.post("/:id/verification", protect, upload.fields([
    { name: 'idDocument', maxCount: 1 },
    { name: 'proofOfArtistry', maxCount: 1 }
]), submitVerificationRequest)
router.get("/:id/verification", protect, getVerificationStatus)
router.put("/:id/verification", protect, admin, updateVerificationStatus)

router.put("/:id/type", protect, admin, updateUserType)
router.put("/:id/approve", protect, admin, approveArtist)
router.put("/:id/suspend", protect, admin, suspendUser)

module.exports = router