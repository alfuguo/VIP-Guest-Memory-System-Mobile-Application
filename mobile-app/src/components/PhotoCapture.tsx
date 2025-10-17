import React, { useState } from 'react';
import { View, StyleSheet, Alert } from 'react-native';
import { Button, Card, Text, ActivityIndicator } from 'react-native-paper';
import { Camera } from 'expo-camera';
import * as ImagePicker from 'expo-image-picker';
import OptimizedImage from './OptimizedImage';
import { optimizeGuestPhoto, getOptimizedImagePickerOptions, formatFileSize } from '../utils/imageUtils';

interface PhotoCaptureProps {
  currentPhotoUrl?: string;
  onPhotoSelected: (uri: string) => void;
  onPhotoRemoved: () => void;
  uploading?: boolean;
}

export default function PhotoCapture({ 
  currentPhotoUrl, 
  onPhotoSelected, 
  onPhotoRemoved,
  uploading = false 
}: PhotoCaptureProps) {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);

  const requestPermissions = async () => {
    const cameraPermission = await Camera.requestCameraPermissionsAsync();
    const mediaLibraryPermission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    
    setHasPermission(
      cameraPermission.status === 'granted' && 
      mediaLibraryPermission.status === 'granted'
    );

    return cameraPermission.status === 'granted' && mediaLibraryPermission.status === 'granted';
  };

  const showImagePicker = async () => {
    const hasPermissions = hasPermission ?? await requestPermissions();
    
    if (!hasPermissions) {
      Alert.alert(
        'Permissions Required',
        'Camera and photo library permissions are required to add photos.',
        [{ text: 'OK' }]
      );
      return;
    }

    Alert.alert(
      'Select Photo',
      'Choose how you want to add a photo',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Take Photo', onPress: takePhoto },
        { text: 'Choose from Library', onPress: pickFromLibrary },
      ]
    );
  };

  const takePhoto = async () => {
    try {
      const result = await ImagePicker.launchCameraAsync(getOptimizedImagePickerOptions());

      if (!result.canceled && result.assets[0]) {
        await processSelectedImage(result.assets[0].uri);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to take photo. Please try again.');
    }
  };

  const pickFromLibrary = async () => {
    try {
      const result = await ImagePicker.launchImageLibraryAsync(getOptimizedImagePickerOptions());

      if (!result.canceled && result.assets[0]) {
        await processSelectedImage(result.assets[0].uri);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to select photo. Please try again.');
    }
  };

  const processSelectedImage = async (uri: string) => {
    try {
      // Show compression progress
      const compressedImage = await optimizeGuestPhoto(uri);
      onPhotoSelected(compressedImage.uri);
    } catch (error) {
      console.error('Image optimization failed:', error);
      // Fall back to original image if optimization fails
      onPhotoSelected(uri);
    }
  };

  const confirmRemovePhoto = () => {
    Alert.alert(
      'Remove Photo',
      'Are you sure you want to remove this photo?',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Remove', style: 'destructive', onPress: onPhotoRemoved },
      ]
    );
  };

  return (
    <Card style={styles.container}>
      <Card.Content>
        <Text style={styles.title}>Guest Photo</Text>
        
        {currentPhotoUrl ? (
          <View style={styles.photoContainer}>
            <OptimizedImage
              uri={currentPhotoUrl}
              size={120}
              style={styles.photo}
              priority="high"
            />
            {uploading && (
              <View style={styles.uploadingOverlay}>
                <ActivityIndicator size="large" color="#fff" />
                <Text style={styles.uploadingText}>Uploading...</Text>
              </View>
            )}
            <View style={styles.photoActions}>
              <Button 
                mode="outlined" 
                onPress={showImagePicker}
                disabled={uploading}
                style={styles.actionButton}
              >
                Change Photo
              </Button>
              <Button 
                mode="text" 
                onPress={confirmRemovePhoto}
                disabled={uploading}
                textColor="#d32f2f"
              >
                Remove
              </Button>
            </View>
          </View>
        ) : (
          <View style={styles.noPhotoContainer}>
            <Text style={styles.noPhotoText}>No photo added</Text>
            <Button 
              mode="contained" 
              onPress={showImagePicker}
              disabled={uploading}
              icon="camera"
              style={styles.addPhotoButton}
            >
              Add Photo
            </Button>
          </View>
        )}
      </Card.Content>
    </Card>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
    elevation: 1,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#333',
  },
  photoContainer: {
    alignItems: 'center',
  },
  photo: {
    marginBottom: 12,
  },
  uploadingOverlay: {
    position: 'absolute',
    top: 0,
    left: '50%',
    marginLeft: -60,
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  uploadingText: {
    color: '#fff',
    marginTop: 8,
    fontSize: 12,
  },
  photoActions: {
    flexDirection: 'row',
    gap: 12,
    alignItems: 'center',
  },
  actionButton: {
    minWidth: 120,
  },
  noPhotoContainer: {
    alignItems: 'center',
    paddingVertical: 20,
  },
  noPhotoText: {
    fontSize: 16,
    color: '#666',
    marginBottom: 16,
  },
  addPhotoButton: {
    minWidth: 140,
  },
});