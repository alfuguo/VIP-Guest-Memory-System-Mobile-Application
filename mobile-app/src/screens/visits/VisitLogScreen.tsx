import React, { useState, useEffect, useCallback } from 'react';
import { 
  View, 
  ScrollView, 
  StyleSheet, 
  Alert,
  KeyboardAvoidingView,
  Platform 
} from 'react-native';
import { 
  Text, 
  TextInput, 
  Button, 
  Card, 
  HelperText,
  Snackbar,
  ActivityIndicator 
} from 'react-native-paper';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { DatePickerModal, TimePickerModal } from 'react-native-paper-dates';

import { Guest } from '../../types/guest';
import { CreateVisitRequest } from '../../types/visit';
import { GuestStackParamList } from '../../types/navigation';
import { GuestService } from '../../services/guestService';
import { VisitService } from '../../services/visitService';
import { handleError } from '../../utils/errorHandler';
import { useAuth } from '../../contexts/AuthContext';

type VisitLogNavigationProp = StackNavigationProp<GuestStackParamList, 'VisitLog'>;
type VisitLogRouteProp = RouteProp<GuestStackParamList, 'VisitLog'>;

interface FormData {
  visitDate: Date;
  visitTime: Date;
  partySize: string;
  tableNumber: string;
  serviceNotes: string;
}

interface FormErrors {
  partySize?: string;
  tableNumber?: string;
  serviceNotes?: string;
}

export default function VisitLogScreen() {
  const navigation = useNavigation<VisitLogNavigationProp>();
  const route = useRoute<VisitLogRouteProp>();
  const { guestId } = route.params;
  const { user } = useAuth();

  // State management
  const [guest, setGuest] = useState<Guest | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Form data
  const [formData, setFormData] = useState<FormData>({
    visitDate: new Date(),
    visitTime: new Date(),
    partySize: '2',
    tableNumber: '',
    serviceNotes: '',
  });

  // Form validation errors
  const [errors, setErrors] = useState<FormErrors>({});

  // Load guest data
  useEffect(() => {
    const loadGuest = async () => {
      try {
        setLoading(true);
        const guestData = await GuestService.getGuest(guestId);
        setGuest(guestData);
      } catch (err) {
        const errorMessage = handleError(err);
        setError(errorMessage);
      } finally {
        setLoading(false);
      }
    };

    loadGuest();
  }, [guestId]);

  // Validation functions
  const validateForm = useCallback((): boolean => {
    const newErrors: FormErrors = {};

    // Validate party size
    const partySize = parseInt(formData.partySize, 10);
    if (!formData.partySize.trim()) {
      newErrors.partySize = 'Party size is required';
    } else if (isNaN(partySize) || partySize < 1 || partySize > 20) {
      newErrors.partySize = 'Party size must be between 1 and 20';
    }

    // Validate table number (optional but if provided, should be reasonable)
    if (formData.tableNumber.trim() && formData.tableNumber.length > 10) {
      newErrors.tableNumber = 'Table number should be 10 characters or less';
    }

    // Validate service notes (optional but if provided, should be reasonable length)
    if (formData.serviceNotes.trim() && formData.serviceNotes.length > 500) {
      newErrors.serviceNotes = 'Service notes should be 500 characters or less';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [formData]);

  // Handle form submission
  const handleSubmit = useCallback(async () => {
    if (!validateForm()) {
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      // Format date and time for API
      const visitDate = formData.visitDate.toISOString().split('T')[0]; // YYYY-MM-DD
      const visitTime = formData.visitTime.toTimeString().split(' ')[0]; // HH:MM:SS

      const visitData: CreateVisitRequest = {
        guestId,
        visitDate,
        visitTime,
        partySize: parseInt(formData.partySize, 10),
        tableNumber: formData.tableNumber.trim() || undefined,
        serviceNotes: formData.serviceNotes.trim() || undefined,
      };

      await VisitService.createVisit(visitData);
      
      setSuccess('Visit logged successfully!');
      
      // Navigate back after a short delay
      setTimeout(() => {
        navigation.goBack();
      }, 1500);

    } catch (err) {
      const errorMessage = handleError(err);
      setError(errorMessage);
    } finally {
      setSubmitting(false);
    }
  }, [formData, guestId, navigation, validateForm]);

  // Handle date change
  const handleDateChange = useCallback((params: any) => {
    setShowDatePicker(false);
    if (params.date) {
      setFormData(prev => ({ ...prev, visitDate: params.date }));
    }
  }, []);

  // Handle time change
  const handleTimeChange = useCallback((params: { hours: number; minutes: number }) => {
    setShowTimePicker(false);
    const newTime = new Date();
    newTime.setHours(params.hours, params.minutes, 0, 0);
    setFormData(prev => ({ ...prev, visitTime: newTime }));
  }, []);

  // Format date for display
  const formatDate = (date: Date): string => {
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  // Format time for display
  const formatTime = (date: Date): string => {
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#6200ee" />
        <Text style={styles.loadingText}>Loading guest information...</Text>
      </View>
    );
  }

  if (!guest) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>Unable to load guest information</Text>
        <Button mode="outlined" onPress={() => navigation.goBack()}>
          Go Back
        </Button>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView 
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView 
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        {/* Guest Information Header */}
        <Card style={styles.guestCard}>
          <Card.Content>
            <Text style={styles.guestName}>
              {guest.firstName} {guest.lastName}
            </Text>
            <Text style={styles.guestPhone}>{guest.phone}</Text>
            {user && (
              <Text style={styles.serverInfo}>
                Served by: {user.firstName} {user.lastName}
              </Text>
            )}
          </Card.Content>
        </Card>

        {/* Visit Form */}
        <Card style={styles.formCard}>
          <Card.Content>
            <Text style={styles.formTitle}>Visit Details</Text>

            {/* Date Selection */}
            <View style={styles.inputGroup}>
              <Text style={styles.label}>Visit Date</Text>
              <Button
                mode="outlined"
                onPress={() => setShowDatePicker(true)}
                style={styles.dateTimeButton}
                contentStyle={styles.dateTimeButtonContent}
              >
                {formatDate(formData.visitDate)}
              </Button>
            </View>

            {/* Time Selection */}
            <View style={styles.inputGroup}>
              <Text style={styles.label}>Visit Time</Text>
              <Button
                mode="outlined"
                onPress={() => setShowTimePicker(true)}
                style={styles.dateTimeButton}
                contentStyle={styles.dateTimeButtonContent}
              >
                {formatTime(formData.visitTime)}
              </Button>
            </View>

            {/* Party Size */}
            <View style={styles.inputGroup}>
              <TextInput
                label="Party Size *"
                value={formData.partySize}
                onChangeText={(text) => setFormData(prev => ({ ...prev, partySize: text }))}
                keyboardType="numeric"
                error={!!errors.partySize}
                style={styles.textInput}
              />
              <HelperText type="error" visible={!!errors.partySize}>
                {errors.partySize}
              </HelperText>
            </View>

            {/* Table Number */}
            <View style={styles.inputGroup}>
              <TextInput
                label="Table Number (Optional)"
                value={formData.tableNumber}
                onChangeText={(text) => setFormData(prev => ({ ...prev, tableNumber: text }))}
                error={!!errors.tableNumber}
                style={styles.textInput}
                placeholder="e.g., A5, 12, Patio 3"
              />
              <HelperText type="error" visible={!!errors.tableNumber}>
                {errors.tableNumber}
              </HelperText>
            </View>

            {/* Service Notes */}
            <View style={styles.inputGroup}>
              <TextInput
                label="Service Notes (Optional)"
                value={formData.serviceNotes}
                onChangeText={(text) => setFormData(prev => ({ ...prev, serviceNotes: text }))}
                multiline
                numberOfLines={4}
                error={!!errors.serviceNotes}
                style={styles.textInput}
                placeholder="Any special requests, preferences noted, or memorable moments..."
              />
              <HelperText type="error" visible={!!errors.serviceNotes}>
                {errors.serviceNotes}
              </HelperText>
              <HelperText type="info" visible={!errors.serviceNotes}>
                {formData.serviceNotes.length}/500 characters
              </HelperText>
            </View>
          </Card.Content>
        </Card>

        {/* Action Buttons */}
        <View style={styles.buttonContainer}>
          <Button
            mode="outlined"
            onPress={() => navigation.goBack()}
            style={styles.cancelButton}
            disabled={submitting}
          >
            Cancel
          </Button>
          <Button
            mode="contained"
            onPress={handleSubmit}
            style={styles.submitButton}
            loading={submitting}
            disabled={submitting}
          >
            {submitting ? 'Logging Visit...' : 'Log Visit'}
          </Button>
        </View>
      </ScrollView>

      {/* Date Picker */}
      <DatePickerModal
        locale="en"
        mode="single"
        visible={showDatePicker}
        onDismiss={() => setShowDatePicker(false)}
        date={formData.visitDate}
        onConfirm={handleDateChange}
        validRange={{
          endDate: new Date(),
        }}
      />

      {/* Time Picker */}
      <TimePickerModal
        visible={showTimePicker}
        onDismiss={() => setShowTimePicker(false)}
        onConfirm={handleTimeChange}
        hours={formData.visitTime.getHours()}
        minutes={formData.visitTime.getMinutes()}
      />

      {/* Success Snackbar */}
      <Snackbar
        visible={!!success}
        onDismiss={() => setSuccess(null)}
        duration={3000}
        style={styles.successSnackbar}
      >
        {success}
      </Snackbar>

      {/* Error Snackbar */}
      <Snackbar
        visible={!!error}
        onDismiss={() => setError(null)}
        duration={4000}
        action={{
          label: 'Retry',
          onPress: handleSubmit,
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
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 32,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorText: {
    fontSize: 16,
    color: '#d32f2f',
    marginBottom: 16,
    textAlign: 'center',
  },
  guestCard: {
    marginBottom: 16,
    elevation: 2,
  },
  guestName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  guestPhone: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  serverInfo: {
    fontSize: 14,
    color: '#6200ee',
    fontWeight: '500',
  },
  formCard: {
    marginBottom: 16,
    elevation: 2,
  },
  formTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
  },
  inputGroup: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: '#333',
    marginBottom: 8,
  },
  textInput: {
    backgroundColor: '#fff',
  },
  dateTimeButton: {
    justifyContent: 'flex-start',
    backgroundColor: '#fff',
    borderColor: '#ccc',
  },
  dateTimeButtonContent: {
    justifyContent: 'flex-start',
    paddingVertical: 8,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
  },
  cancelButton: {
    flex: 1,
    marginRight: 8,
  },
  submitButton: {
    flex: 1,
    marginLeft: 8,
    backgroundColor: '#6200ee',
  },
  successSnackbar: {
    backgroundColor: '#4caf50',
  },
});