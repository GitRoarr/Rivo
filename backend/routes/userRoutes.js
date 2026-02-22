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
    getAllUsers,
    getUsersAwaitingVerification,
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

// ── Routes parameterised by userId (:id) ─────────────────────────────────────
router.get("/:id", protect, getUserById)
router.delete("/:id", protect, deleteUser)

// Follow / unfollow
router.post("/:id/follow", protect, followUser)
router.delete("/:id/follow", protect, unfollowUser)
router.get("/:id/followers", protect, getFollowers)
router.get("/:id/following", protect, getFollowing)

// Verification (artist submits, admin reviews)
router.post("/:id/verification", protect, submitVerificationRequest)
router.get("/:id/verification", protect, getVerificationStatus)
router.put("/:id/verification", protect, admin, updateVerificationStatus)

// Admin-only management
router.get("/", protect, admin, getAllUsers)
router.get("/verification/pending", protect, admin, getUsersAwaitingVerification)
router.put("/:id/type", protect, admin, updateUserType)
router.put("/:id/approve", protect, admin, approveArtist)
router.put("/:id/suspend", protect, admin, suspendUser)

module.exports = router