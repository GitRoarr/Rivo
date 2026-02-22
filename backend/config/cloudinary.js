const cloudinary = require('cloudinary').v2;
const { CloudinaryStorage } = require('multer-storage-cloudinary');
const multer = require('multer');
const dotenv = require('dotenv');

dotenv.config();

cloudinary.config({
    cloud_name: (process.env.CLOUDINARY_CLOUD_NAME || "").trim(),
    api_key: (process.env.CLOUDINARY_API_KEY || "").trim(),
    api_secret: (process.env.CLOUDINARY_API_SECRET || "").trim(),
});

const storage = new CloudinaryStorage({
    cloudinary: cloudinary,
    params: async (req, file) => {
        const fileExtension = file.originalname.split('.').pop().toLowerCase();

        const cleanFileName = file.originalname.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z0-9]/g, "_");

        if (file.fieldname === 'audio') {
            return {
                folder: 'rivo/audio',
                resource_type: 'video', // Must be video for audio files
                format: fileExtension === 'mp3' ? 'mp3' : 'm4a', // Keep original format if possible
                public_id: `${Date.now()}-${cleanFileName}`
            };
        } else if (file.fieldname === 'profileImage') {
            return {
                folder: 'rivo/profiles',
                resource_type: 'image',
                transformation: [{ width: 500, height: 500, crop: 'fill', gravity: 'face' }],
                public_id: `${Date.now()}-${cleanFileName}`
            };
        } else if (file.fieldname === 'coverImage') {
            return {
                folder: 'rivo/covers',
                resource_type: 'image',
                transformation: [{ width: 1200, height: 600, crop: 'limit' }],
                public_id: `${Date.now()}-${cleanFileName}`
            };
        }
        else {
            return {
                folder: 'rivo/others',
                resource_type: 'auto',
                public_id: `${Date.now()}-${cleanFileName}`
            };
        }
    },
});

const upload = multer({
    storage: storage,
    limits: {
        fileSize: 50 * 1024 * 1024, // 50MB limit
    }
});

module.exports = {
    cloudinary,
    upload
};
