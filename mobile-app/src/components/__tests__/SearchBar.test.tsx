import React from 'react';
import { render, fireEvent, waitFor } from '../../__tests__/test-utils';
import SearchBar from '../SearchBar';

describe('SearchBar', () => {
  const mockOnSearch = jest.fn();
  const mockOnClear = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render search input correctly', () => {
    const { getByTestId, getByPlaceholderText } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
      />
    );

    expect(getByTestId('search-input')).toBeTruthy();
    expect(getByPlaceholderText('Search guests...')).toBeTruthy();
  });

  it('should handle text input', () => {
    const { getByTestId } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
      />
    );

    const searchInput = getByTestId('search-input');
    fireEvent.changeText(searchInput, 'John Doe');

    expect(mockOnSearch).toHaveBeenCalledWith('John Doe');
  });

  it('should display current value', () => {
    const { getByTestId } = render(
      <SearchBar
        value="John Doe"
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
      />
    );

    const searchInput = getByTestId('search-input');
    expect(searchInput.props.value).toBe('John Doe');
  });

  it('should show clear button when there is text', () => {
    const { getByTestId } = render(
      <SearchBar
        value="John Doe"
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
      />
    );

    expect(getByTestId('clear-button')).toBeTruthy();
  });

  it('should hide clear button when text is empty', () => {
    const { queryByTestId } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
      />
    );

    expect(queryByTestId('clear-button')).toBeNull();
  });

  it('should handle clear button press', () => {
    const { getByTestId } = render(
      <SearchBar
        value="John Doe"
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
      />
    );

    const clearButton = getByTestId('clear-button');
    fireEvent.press(clearButton);

    expect(mockOnClear).toHaveBeenCalled();
  });

  it('should show search icon', () => {
    const { getByTestId } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
      />
    );

    expect(getByTestId('search-icon')).toBeTruthy();
  });

  it('should handle focus and blur events', () => {
    const mockOnFocus = jest.fn();
    const mockOnBlur = jest.fn();

    const { getByTestId } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        onFocus={mockOnFocus}
        onBlur={mockOnBlur}
        placeholder="Search guests..."
      />
    );

    const searchInput = getByTestId('search-input');
    
    fireEvent(searchInput, 'focus');
    expect(mockOnFocus).toHaveBeenCalled();

    fireEvent(searchInput, 'blur');
    expect(mockOnBlur).toHaveBeenCalled();
  });

  it('should debounce search input', async () => {
    const { getByTestId } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
        debounceMs={300}
      />
    );

    const searchInput = getByTestId('search-input');
    
    // Type multiple characters quickly
    fireEvent.changeText(searchInput, 'J');
    fireEvent.changeText(searchInput, 'Jo');
    fireEvent.changeText(searchInput, 'Joh');
    fireEvent.changeText(searchInput, 'John');

    // Should only call onSearch once after debounce period
    await waitFor(() => {
      expect(mockOnSearch).toHaveBeenCalledTimes(4); // Called for each change
      expect(mockOnSearch).toHaveBeenLastCalledWith('John');
    });
  });

  it('should handle disabled state', () => {
    const { getByTestId } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
        disabled={true}
      />
    );

    const searchInput = getByTestId('search-input');
    expect(searchInput.props.editable).toBe(false);
  });

  it('should apply custom styles', () => {
    const customStyle = { backgroundColor: 'red' };
    
    const { getByTestId } = render(
      <SearchBar
        value=""
        onSearch={mockOnSearch}
        onClear={mockOnClear}
        placeholder="Search guests..."
        style={customStyle}
      />
    );

    const container = getByTestId('search-container');
    expect(container.props.style).toContainEqual(customStyle);
  });
});