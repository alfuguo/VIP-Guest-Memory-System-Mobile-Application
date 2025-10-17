import React, { useState, useEffect, useCallback } from 'react';
import { 
  View, 
  StyleSheet, 
  ScrollView, 
  Alert,
  KeyboardAvoidingView,
  Platform 
} from 'react-native';
import { 
  TextInput, 
  Button, 
  Appbar,
  Snackbar,
  Card,
  Text 
} from 'react-native-paper';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useForm, Controller } from 'react-hook-form';

import { Guest, CreateGuestRequest, UpdateGuestRequest } from '../../types/guest';
import { GuestStackParamList } from '../../types/navigation';
import { GuestService } from '../../services/guestService';
import { handleError } from '../../utils/errorHandler';

import PhotoCapture from '../../components/PhotoCapture';
import GuestPreferencesForm from '../../components/GuestPreferencesForm';
import LoadingState from '../../components/LoadingState';

type GuestProfileNavigationProp = StackNavigationProp<GuestStackParamList, 'GuestProfile'>;
type GuestProfileRouteProp = RouteProp<GuestStackParamList, 'GuestProfile'>;

interface GuestFormData {
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
}

interface GuestPreferences {
  seatingPreference?: string;
  dietaryRestrictions: string[];
  favoriteDrinks: string[];
  birthday?: string;
  anniversary?: string;
  notes?: string;
}

export default function GuestProfileScreen() {
  const navigation = useNavigation<GuestProfileNavigationProp>();
  const route = useRoute<GuestProfileRouteProp>();
  const { guestId } = route.params;

  // State management
  const [guest, setGuest] = useState<Guest | null>(null);
  const [loading, setLoading] = useState(!!guestId);
  const [saving, setSaving] = useState(false);
  const [photoUploading, setPhotoUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [photoUri, setPhotoUri] = useState<string | undefined>();

  // Form management
  const { control, handleSubmit, formState: { errors }, setValue, watch } = useForm<GuestFormData>({
    defaultValues: {
      firstName: '',
      lastName: '',
      phone: '',
      email: '',
    }
  });

  // Preferences state
  const [preferences, setPreferences] = useState<GuestPreferences>({
    dietaryRestrictions: [],
    favoriteDrinks: [],
  });

  const isEditing = !!guestId;
  const watchedPhone = watch('phone');

  // Load guest data if editing
  useEffect(() => {
    if (guestId) {
      loadGuest();
    }
  }, [guestId]);

  const loadGuest = async () => {
    try {
      setLoading(true);
      const guestData = await GuestService.getGuest(guestId!);
      
      setGuest(guestData);
      
      // Populate form
      setValue('firstName', guestData.firstName);
      setValue('lastName', guestData.lastName || '');
      setValue('phone', guestData.phone);
      setValue('email', guestData.email || '');
      
      // Populate preferences
      setPreferences({
        seatingPreference: guestData.seatingPreference,
        dietaryRestrictions: guestData.dietaryRestrictions,
        favoriteDrinks: guestData.favoriteDrinks,
        birthday: guestData.birthday,
        anniversary: guestData.anniversary,
        notes: guestData.notes,
      });

      setPhotoUri(guestData.photoUrl);
      
    } catch (err) {
      const errorMessage = handleError(err);
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // Handle form submission
  const onSubmit = async (formData: GuestFormData) => {
    try {
      setSaving(true);
      setError(null);

      const guestData = {
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim() || undefined,
        phone: formData.phone.trim(),
        email: formData.email.trim() || undefined,
        seatingPreference: preferences.seatingPreference,
        dietaryRestrictions: preferences.dietaryRestrictions,
        favoriteDrinks: preferences.favoriteDrinks,
        birthday: preferences.birthday,
        anniversary: preferences.anniversary,
        notes: preferences.notes?.trim() || undefined,
      };

      let savedGuest: Guest;

      if (isEditing) {
        savedGuest = await GuestService.updateGuest({
          id: guestId!,
          ...guestData,
        } as UpdateGuestRequest);
        setSuccess('Guest updated successfully');
      } else {
        savedGuest = await GuestService.createGuest(guestData as CreateGuestRequest);
        setSuccess('Guest created successfully');
      }

      setGuest(savedGuest);

      // Upload photo if selected
      if (photoUri && !photoUri.startsWith('http')) {
        await uploadPhoto(savedGuest.id, photoUri);
      }

      // Navigate back after a short delay
      setTimeout(() => {
        navigation.goBack();
      }, 1500);

    } catch (err) {
      const errorMessage = handleError(err);
      setError(errorMessage);
    } finally {
      setSaving(false);
    }
  };

  // Handle photo upload
  const uploadPhoto = async (guestId: number, uri: string) => {
    try {
      setPhotoUploading(true);
      const response = await GuestService.uploadGuestPhoto(
        guestId, 
        uri, 
        (progress) => {
          // Progress callback could be used to show upload progress
        }
      );
      setPhotoUri(response.photoUrl);
    } catch (err) {
      const errorMessage = handleError(err);
      Alert.alert('Photo Upload Failed', errorMessage);
    } finally {
      setPhotoUploading(false);
    }
  };

  // Handle photo selection
  const handlePhotoSelected = useCallback((uri: string) => {
    setPhotoUri(uri);
  }, []);

  // Handle photo removal
  const handlePhotoRemoved = useCallback(() => {
    setPhotoUri(undefined);
  }, []);

  // Handle preferences change
  const handlePreferencesChange = useCallback((newPreferences: GuestPreferences) => {
    setPreferences(newPreferences);
  }, []);

  // Validate phone number format
  const validatePhone = (phone: string) => {
    const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
    return phoneRegex.test(phone.replace(/[\s\-\(\)]/g, '')) || 'Please enter a valid phone number';
  };

  // Validate email format
  const validateEmail = (email: string) => {
    if (!email.trim()) return true; // Email is optional
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email) || 'Please enter a valid email address';
  };

  // Show loading state
  if (loading) {
    return <LoadingState message="Loading guest..." />;
  }

  return (
    <KeyboardAvoidingView 
      style={styles.container} 
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <Appbar.Header>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title={isEditing ? 'Edit Guest' : 'New Guest'} />
        <Appbar.Action 
          icon="check" 
          onPress={handleSubmit(onSubmit)}
          disabled={saving}
        />
      </Appbar.Header>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {/* Basic Information */}
        <Card style={styles.section}>
          <Card.Content>
            <Text style={styles.sectionTitle}>Basic Information</Text>
            
            <Controller
              control={control}
              name="firstName"
              rules={{ required: 'First name is required' }}
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="First Name *"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={!!errors.firstName}
                  mode="outlined"
                  style={styles.input}
                />
              )}
            />
            {errors.firstName && (
              <Text style={styles.errorText}>{errors.firstName.message}</Text>
            )}

            <Controller
              control={control}
              name="lastName"
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="Last Name"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  mode="outlined"
                  style={styles.input}
                />
              )}
            />

            <Controller
              control={control}
              name="phone"
              rules={{ 
                required: 'Phone number is required',
                validate: validatePhone
              }}
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="Phone Number *"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={!!errors.phone}
                  mode="outlined"
                  style={styles.input}
                  keyboardType="phone-pad"
                />
              )}
            />
            {errors.phone && (
              <Text style={styles.errorText}>{errors.phone.message}</Text>
            )}

            <Controller
              control={control}
              name="email"
              rules={{ validate: validateEmail }}
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="Email"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={!!errors.email}
                  mode="outlined"
                  style={styles.input}
                  keyboardType="email-address"
                  autoCapitalize="none"
                />
              )}
            />
            {errors.email && (
              <Text style={styles.errorText}>{errors.email.message}</Text>
            )}
          </Card.Content>
        </Card>

        {/* Photo */}
        <PhotoCapture
          currentPhotoUrl={photoUri}
          onPhotoSelected={handlePhotoSelected}
          onPhotoRemoved={handlePhotoRemoved}
          uploading={photoUploading}
        />

        {/* Preferences */}
        <GuestPreferencesForm
          preferences={preferences}
          onPreferencesChange={handlePreferencesChange}
        />

        {/* Save Button */}
        <Button
          mode="contained"
          onPress={handleSubmit(onSubmit)}
          loading={saving}
          disabled={saving}
          style={styles.saveButton}
          contentStyle={styles.saveButtonContent}
        >
          {saving ? 'Saving...' : (isEditing ? 'Update Guest' : 'Create Guest')}
        </Button>

        <View style={styles.bottomSpacing} />
      </ScrollView>

      {/* Success/Error Messages */}
      <Snackbar
        visible={!!success}
        onDismiss={() => setSuccess(null)}
        duration={3000}
        style={styles.successSnackbar}
      >
        {success}
      </Snackbar>

      <Snackbar
        visible={!!error}
        onDismiss={() => setError(null)}
        duration={4000}
        action={{
          label: 'Dismiss',
          onPress: () => setError(null),
        }}
      >
        {error}
      </Snackbar>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  content: {
    flex: 1,
    padding: 16,
  },
  section: {
    marginBottom: 16,
    elevation: 1,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#333',
  },
  input: {
    marginBottom: 12,
  },
  errorText: {
    color: '#d32f2f',
    fontSize: 12,
    marginTop: -8,
    marginBottom: 8,
    marginLeft: 12,
  },
  saveButton: {
    marginTop: 24,
    marginBottom: 16,
  },
  saveButtonContent: {
    paddingVertical: 8,
  },
  bottomSpacing: {
    height: 40,
  },
  successSnackbar: {
    backgroundColor: '#4caf50',
  },
});