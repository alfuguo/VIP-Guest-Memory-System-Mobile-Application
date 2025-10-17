import React from 'react';
import { render, fireEvent } from '../../__tests__/test-utils';
import GuestCard from '../GuestCard';
import { mockGuest } from '../../__tests__/test-utils';

describe('GuestCard', () => {
  const mockOnPress = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render guest information correctly', () => {
    const { getByText, getByTestId } = render(
      <GuestCard guest={mockGuest} onPress={mockOnPress} />
    );

    expect(getByText('John Doe')).toBeTruthy();
    expect(getByText('+1234567890')).toBeTruthy();
    expect(getByText('12 visits')).toBeTruthy();
    expect(getByTestId('guest-photo')).toBeTruthy();
  });

  it('should handle guest without last name', () => {
    const guestWithoutLastName = {
      ...mockGuest,
      lastName: undefined,
    };

    const { getByText } = render(
      <GuestCard guest={guestWithoutLastName} onPress={mockOnPress} />
    );

    expect(getByText('John')).toBeTruthy();
  });

  it('should display last visit date', () => {
    const { getByText } = render(
      <GuestCard guest={mockGuest} onPress={mockOnPress} />
    );

    expect(getByText('Last visit: Jan 15, 2024')).toBeTruthy();
  });

  it('should display dietary restrictions', () => {
    const { getByText } = render(
      <GuestCard guest={mockGuest} onPress={mockOnPress} />
    );

    expect(getByText('Vegetarian')).toBeTruthy();
  });

  it('should display favorite drinks', () => {
    const { getByText } = render(
      <GuestCard guest={mockGuest} onPress={mockOnPress} />
    );

    expect(getByText('Red wine')).toBeTruthy();
  });

  it('should handle press event', () => {
    const { getByTestId } = render(
      <GuestCard guest={mockGuest} onPress={mockOnPress} />
    );

    const card = getByTestId('guest-card');
    fireEvent.press(card);

    expect(mockOnPress).toHaveBeenCalledWith(mockGuest);
  });

  it('should show placeholder when no photo available', () => {
    const guestWithoutPhoto = {
      ...mockGuest,
      photoUrl: undefined,
    };

    const { getByTestId } = render(
      <GuestCard guest={guestWithoutPhoto} onPress={mockOnPress} />
    );

    const photo = getByTestId('guest-photo');
    expect(photo.props.source).toEqual({ uri: undefined });
  });

  it('should display special occasion indicator for birthday', () => {
    const today = new Date();
    const birthdayGuest = {
      ...mockGuest,
      birthday: today.toISOString().split('T')[0], // Today's date
    };

    const { getByTestId } = render(
      <GuestCard guest={birthdayGuest} onPress={mockOnPress} />
    );

    expect(getByTestId('birthday-indicator')).toBeTruthy();
  });

  it('should display special occasion indicator for anniversary', () => {
    const today = new Date();
    const anniversaryGuest = {
      ...mockGuest,
      anniversary: today.toISOString().split('T')[0], // Today's date
    };

    const { getByTestId } = render(
      <GuestCard guest={anniversaryGuest} onPress={mockOnPress} />
    );

    expect(getByTestId('anniversary-indicator')).toBeTruthy();
  });

  it('should handle guest with no visits', () => {
    const newGuest = {
      ...mockGuest,
      visitCount: 0,
      lastVisit: undefined,
    };

    const { getByText } = render(
      <GuestCard guest={newGuest} onPress={mockOnPress} />
    );

    expect(getByText('0 visits')).toBeTruthy();
    expect(getByText('No previous visits')).toBeTruthy();
  });

  it('should display seating preference', () => {
    const { getByText } = render(
      <GuestCard guest={mockGuest} onPress={mockOnPress} />
    );

    expect(getByText('Prefers: Window table')).toBeTruthy();
  });

  it('should handle long guest names gracefully', () => {
    const guestWithLongName = {
      ...mockGuest,
      firstName: 'VeryLongFirstNameThatShouldBeTruncated',
      lastName: 'VeryLongLastNameThatShouldAlsoBeTruncated',
    };

    const { getByText } = render(
      <GuestCard guest={guestWithLongName} onPress={mockOnPress} />
    );

    // Should still render the name (truncation is handled by styling)
    expect(getByText('VeryLongFirstNameThatShouldBeTruncated VeryLongLastNameThatShouldAlsoBeTruncated')).toBeTruthy();
  });
});