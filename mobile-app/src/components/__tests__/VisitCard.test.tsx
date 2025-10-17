import React from 'react';
import { render, fireEvent } from '../../__tests__/test-utils';
import VisitCard from '../VisitCard';
import { mockVisit } from '../../__tests__/test-utils';

describe('VisitCard', () => {
  const mockOnPress = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render visit information correctly', () => {
    const { getByText, getByTestId } = render(
      <VisitCard visit={mockVisit} onPress={mockOnPress} />
    );

    expect(getByText('Jan 15, 2024')).toBeTruthy();
    expect(getByText('7:30 PM')).toBeTruthy();
    expect(getByText('Party of 2')).toBeTruthy();
    expect(getByText('Table A5')).toBeTruthy();
    expect(getByText('Server: Jane Smith')).toBeTruthy();
    expect(getByTestId('visit-card')).toBeTruthy();
  });

  it('should display service notes when available', () => {
    const { getByText } = render(
      <VisitCard visit={mockVisit} onPress={mockOnPress} />
    );

    expect(getByText('Celebrated anniversary')).toBeTruthy();
  });

  it('should handle visit without service notes', () => {
    const visitWithoutNotes = {
      ...mockVisit,
      serviceNotes: undefined,
    };

    const { queryByText } = render(
      <VisitCard visit={visitWithoutNotes} onPress={mockOnPress} />
    );

    expect(queryByText('Celebrated anniversary')).toBeNull();
  });

  it('should handle visit without table number', () => {
    const visitWithoutTable = {
      ...mockVisit,
      tableNumber: undefined,
    };

    const { queryByText } = render(
      <VisitCard visit={visitWithoutTable} onPress={mockOnPress} />
    );

    expect(queryByText('Table A5')).toBeNull();
  });

  it('should handle press event', () => {
    const { getByTestId } = render(
      <VisitCard visit={mockVisit} onPress={mockOnPress} />
    );

    const card = getByTestId('visit-card');
    fireEvent.press(card);

    expect(mockOnPress).toHaveBeenCalledWith(mockVisit);
  });

  it('should display party size correctly', () => {
    const largePartyVisit = {
      ...mockVisit,
      partySize: 8,
    };

    const { getByText } = render(
      <VisitCard visit={largePartyVisit} onPress={mockOnPress} />
    );

    expect(getByText('Party of 8')).toBeTruthy();
  });

  it('should handle single person party', () => {
    const singlePartyVisit = {
      ...mockVisit,
      partySize: 1,
    };

    const { getByText } = render(
      <VisitCard visit={singlePartyVisit} onPress={mockOnPress} />
    );

    expect(getByText('Party of 1')).toBeTruthy();
  });

  it('should format time correctly for different times', () => {
    const morningVisit = {
      ...mockVisit,
      visitTime: '09:30:00',
    };

    const { getByText } = render(
      <VisitCard visit={morningVisit} onPress={mockOnPress} />
    );

    expect(getByText('9:30 AM')).toBeTruthy();
  });

  it('should format date correctly for different dates', () => {
    const differentDateVisit = {
      ...mockVisit,
      visitDate: '2023-12-25',
    };

    const { getByText } = render(
      <VisitCard visit={differentDateVisit} onPress={mockOnPress} />
    );

    expect(getByText('Dec 25, 2023')).toBeTruthy();
  });

  it('should show visit duration if provided', () => {
    const visitWithDuration = {
      ...mockVisit,
      duration: 120, // 2 hours in minutes
    };

    const { getByText } = render(
      <VisitCard visit={visitWithDuration} onPress={mockOnPress} />
    );

    expect(getByText('Duration: 2h 0m')).toBeTruthy();
  });

  it('should handle visit without staff name', () => {
    const visitWithoutStaff = {
      ...mockVisit,
      staffName: undefined,
    };

    const { queryByText } = render(
      <VisitCard visit={visitWithoutStaff} onPress={mockOnPress} />
    );

    expect(queryByText('Server: Jane Smith')).toBeNull();
  });

  it('should show edit indicator for recent visits', () => {
    const recentVisit = {
      ...mockVisit,
      createdAt: new Date().toISOString(), // Very recent
    };

    const { getByTestId } = render(
      <VisitCard visit={recentVisit} onPress={mockOnPress} canEdit={true} />
    );

    expect(getByTestId('edit-indicator')).toBeTruthy();
  });

  it('should not show edit indicator when canEdit is false', () => {
    const { queryByTestId } = render(
      <VisitCard visit={mockVisit} onPress={mockOnPress} canEdit={false} />
    );

    expect(queryByTestId('edit-indicator')).toBeNull();
  });
});