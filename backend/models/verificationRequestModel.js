const mongoose = require('mongoose');

const verificationRequestSchema = mongoose.Schema({
    userId: {
        type: String,
        required: true,
        ref: 'User'
    },
    artistName: { type: String, required: true },
    email: { type: String, required: true },
    phoneNumber: { type: String, required: true },
    location: { type: String, required: true },
    primaryGenre: { type: String, required: true },
    artistBio: { type: String, required: true },
    socialLinks: { type: Map, of: String },
    idDocumentUrl: { type: String, required: true }, // from Cloudinary
    proofOfArtistryUrl: { type: String, required: true }, // from Cloudinary
    status: {
        type: String,
        enum: ['PENDING', 'APPROVED', 'REJECTED'],
        default: 'PENDING'
    },
    rejectionReason: { type: String }
}, { timestamps: true });

const VerificationRequest = mongoose.model('VerificationRequest', verificationRequestSchema);

module.exports = VerificationRequest;
