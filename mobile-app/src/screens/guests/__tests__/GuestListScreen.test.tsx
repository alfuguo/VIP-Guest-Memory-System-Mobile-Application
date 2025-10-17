import React from 'react';
import { render, fireEvent, waitFor } from '../../../__tests__/test-utils';
import GuestListScreen from '../GuestListScreen';
import { useQuery } from '@tanstack/react-query';
import { mockGuest, createMockNavigation, createMockRoute } from '../../../__tests__/test-utils';

// Mock React Query
jest.mock('@tanstack/react-query');
const mockUseQuery = useQuery as jest.MockedFunction<typeof useQuery>;

// Mock the guest service
jest.mock('../../../services/guestService', () => ({
  searchGuests: jest.fn(),
  getGuests: jest.fn(),
}));

describe('GuestListScreen', () => {
  const mockNavigation = createMockNavigation();
  const mockRoute = createMockRoute();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render guest list correctly', () => {
    const mockGuests = [mockGuest];

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 1 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByText, getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    expect(getByText('Guests')).toBeTruthy();
    expect(getByTestId('search-bar')).toBeTruthy();
    expect(getByText('John Doe')).toBeTruthy();
  });

  it('should show loading state', () => {
    mockUseQuery.mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    expect(getByTestId('loading-skeleton')).toBeTruthy();
  });

  it('should show error state', () => {
    mockUseQuery.mockReturnValue({
      data: undefined,
      isLoading: false,
      error: new Error('Failed to load guests'),
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByText, getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    expect(getByTestId('error-state')).toBeTruthy();
    expect(getByText('Failed to load guests')).toBeTruthy();
  });

  it('should show empty state when no guests found', () => {
    mockUseQuery.mockReturnValue({
      data: { guests: [], totalCount: 0 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByText, getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    expect(getByTestId('empty-state')).toBeTruthy();
    expect(getByText('No guests found')).toBeTruthy();
  });

  it('should handle search input', async () => {
    const mockGuests = [mockGuest];

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 1 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    const searchBar = getByTestId('search-bar');
    fireEvent.changeText(searchBar, 'John');

    // Should trigger search after debounce
    await waitFor(() => {
      expect(mockUseQuery).toHaveBeenCalledWith(
        expect.objectContaining({
          queryKey: expect.arrayContaining(['guests', expect.objectContaining({ search: 'John' })]),
        })
      );
    });
  });

  it('should navigate to guest detail on card press', () => {
    const mockGuests = [mockGuest];

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 1 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    const guestCard = getByTestId('guest-card');
    fireEvent.press(guestCard);

    expect(mockNavigation.navigate).toHaveBeenCalledWith('GuestDetail', {
      guestId: mockGuest.id,
    });
  });

  it('should handle pull to refresh', async () => {
    const mockRefetch = jest.fn();
    const mockGuests = [mockGuest];

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 1 },
      isLoading: false,
      error: null,
      refetch: mockRefetch,
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    const flatList = getByTestId('guest-list');
    fireEvent(flatList, 'refresh');

    expect(mockRefetch).toHaveBeenCalled();
  });

  it('should show add guest button for managers', () => {
    // Mock user role as manager
    const mockGuests = [mockGuest];

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 1 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    expect(getByTestId('add-guest-fab')).toBeTruthy();
  });

  it('should navigate to add guest screen', () => {
    const mockGuests = [mockGuest];

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 1 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    const addButton = getByTestId('add-guest-fab');
    fireEvent.press(addButton);

    expect(mockNavigation.navigate).toHaveBeenCalledWith('GuestProfile', {
      mode: 'create',
    });
  });

  it('should show filter button', () => {
    const mockGuests = [mockGuest];

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 1 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    expect(getByTestId('filter-button')).toBeTruthy();
  });

  it('should handle infinite scroll loading', () => {
    const mockGuests = Array.from({ length: 20 }, (_, i) => ({
      ...mockGuest,
      id: i + 1,
      firstName: `Guest${i + 1}`,
    }));

    mockUseQuery.mockReturnValue({
      data: { guests: mockGuests, totalCount: 50 },
      isLoading: false,
      error: null,
      refetch: jest.fn(),
      isRefetching: false,
    } as any);

    const { getByTestId } = render(
      <GuestListScreen navigation={mockNavigation} route={mockRoute} />
    );

    const flatList = getByTestId('guest-list');
    
    // Simulate reaching end of list
    fireEvent(flatList, 'endReached');

    // Should trigger loading more guests
    expect(mockUseQuery).toHaveBeenCalledWith(
      expect.objectContaining({
        queryKey: expect.arrayContaining(['guests']),
      })
    );
  });
});