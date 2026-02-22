const cloudinary = require('cloudinary').v2;
const dotenv = require('dotenv');
dotenv.config();

cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME.trim(),
    api_key: process.env.CLOUDINARY_API_KEY.trim(),
    api_secret: process.env.CLOUDINARY_API_SECRET.trim(),
});

console.log("Testing Cloudinary with:");
console.log("Cloud Name:", process.env.CLOUDINARY_CLOUD_NAME);
console.log("API Key:", process.env.CLOUDINARY_API_KEY);

// Try a simple authenticated call
cloudinary.api.ping()
    .then(result => {
        console.log("Ping successful:", result);
        process.exit(0);
    })
    .catch(error => {
        console.error("Ping failed:", error);
        process.exit(1);
    });
