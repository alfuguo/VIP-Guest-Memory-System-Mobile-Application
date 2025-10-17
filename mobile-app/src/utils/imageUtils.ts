import * as ImagePicker from 'expo-image-picker';
import { manipulateAsync, SaveFormat } from 'expo-image-manipulator';

export interface ImageCompressionOptions {
  maxWidth?: number;
  maxHeight?: number;
  quality?: number;
  format?: SaveFormat;
}

export interface CompressedImageResult {
  uri: string;
  width: number;
  height: number;
  size?: number;
}

/**
 * Compress and resize an image for optimal storage and transmission
 */
export async function compressImage(
  uri: string,
  options: ImageCompressionOptions = {}
): Promise<CompressedImageResult> {
  const {
    maxWidth = 800,
    maxHeight = 800,
    quality = 0.8,
    format = SaveFormat.JPEG
  } = options;

  try {
    // Get original image info
    const imageInfo = await manipulateAsync(uri, [], { format: SaveFormat.JPEG });
    
    // Calculate resize dimensions while maintaining aspect ratio
    const { width: originalWidth, height: originalHeight } = imageInfo;
    let { width, height } = imageInfo;
    
    if (width > maxWidth || height > maxHeight) {
      const aspectRatio = width / height;
      
      if (width > height) {
        width = Math.min(width, maxWidth);
        height = width / aspectRatio;
      } else {
        height = Math.min(height, maxHeight);
        width = height * aspectRatio;
      }
    }

    // Apply compression and resizing
    const result = await manipulateAsync(
      uri,
      [
        {
          resize: {
            width: Math.round(width),
            height: Math.round(height),
          },
        },
      ],
      {
        compress: quality,
        format,
        base64: false,
      }
    );

    return {
      uri: result.uri,
      width: result.width,
      height: result.height,
    };
  } catch (error) {
    console.error('Image compression failed:', error);
    // Return original if compression fails
    return {
      uri,
      width: 0,
      height: 0,
    };
  }
}

/**
 * Create thumbnail version of an image
 */
export async function createThumbnail(
  uri: string,
  size: number = 150
): Promise<CompressedImageResult> {
  return compressImage(uri, {
    maxWidth: size,
    maxHeight: size,
    quality: 0.7,
    format: SaveFormat.JPEG,
  });
}

/**
 * Optimize image for guest profile photos
 */
export async function optimizeGuestPhoto(uri: string): Promise<CompressedImageResult> {
  return compressImage(uri, {
    maxWidth: 400,
    maxHeight: 400,
    quality: 0.85,
    format: SaveFormat.JPEG,
  });
}

/**
 * Get optimized image picker options
 */
export function getOptimizedImagePickerOptions(): ImagePicker.ImagePickerOptions {
  return {
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    allowsEditing: true,
    aspect: [1, 1],
    quality: 0.8, // Initial quality, will be further optimized
    exif: false, // Remove EXIF data for privacy and size
  };
}

/**
 * Calculate image file size estimate in bytes
 */
export function estimateImageSize(width: number, height: number, quality: number = 0.8): number {
  // Rough estimate: JPEG compression typically achieves 10:1 to 20:1 compression
  const uncompressedSize = width * height * 3; // 3 bytes per pixel (RGB)
  const compressionRatio = quality * 15; // Estimate compression ratio based on quality
  return Math.round(uncompressedSize / compressionRatio);
}

/**
 * Format file size for display
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}