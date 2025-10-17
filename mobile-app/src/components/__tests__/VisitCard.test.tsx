import React from 'react';
import { render, fireEvent } from '../../__tests__/test-utils';
import VisitCard from '../VisitCard';
import { mockVisit } from '../../__tests__/test-utils';

describe('VisitCard', () => {
    const mockOnEdit = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('should render visit information correctly', () => {
        const { getByText } = render(
            <VisitCard visit={mockVisit} />
        );

        expect(getByText(/Jan 15, 2024/)).toBeTruthy();
        expect(getByText(/7:30 PM/)).toBeTruthy();
        expect(getByText(/Party of 2/)).toBeTruthy();
        expect(getByText(/Table A5/)).toBeTruthy();
        expect(getByText(/Jane Smith/)).toBeTruthy();
    });

    it('should display service notes when available', () => {
        const { getByText } = render(
            <VisitCard visit={mockVisit} />
        );

        expect(getByText(/Celebrated anniversary/)).toBeTruthy();
    });

    it('should handle visit without service notes', () => {
        const visitWithoutNotes = {
            ...mockVisit,
            serviceNotes: '',
        };

        const { queryByText } = render(
            <VisitCard visit={visitWithoutNotes} />
        );

        expect(queryByText('Celebrated anniversary')).toBeNull();
    });

    it('should handle visit without table number', () => {
        const visitWithoutTable = {
            ...mockVisit,
            tableNumber: '',
        };

        const { queryByText } = render(
            <VisitCard visit={visitWithoutTable} />
        );

        expect(queryByText(/Table A5/)).toBeNull();
    });

    it('should handle edit functionality when canEdit is true', () => {
        const { getByRole } = render(
            <VisitCard visit={mockVisit} onEdit={mockOnEdit} canEdit={true} />
        );

        const editButton = getByRole('button');
        fireEvent.press(editButton);

        expect(mockOnEdit).toHaveBeenCalledWith(mockVisit);
    });

    it('should display party size correctly', () => {
        const largePartyVisit = {
            ...mockVisit,
            partySize: 8,
        };

        const { getByText } = render(
            <VisitCard visit={largePartyVisit} />
        );

        expect(getByText(/Party of 8/)).toBeTruthy();
    });

    it('should handle single person party', () => {
        const singlePartyVisit = {
            ...mockVisit,
            partySize: 1,
        };

        const { getByText } = render(
            <VisitCard visit={singlePartyVisit} />
        );

        expect(getByText(/Party of 1/)).toBeTruthy();
    });

    it('should format time correctly for different times', () => {
        const morningVisit = {
            ...mockVisit,
            visitTime: '09:30:00',
        };

        const { getByText } = render(
            <VisitCard visit={morningVisit} />
        );

        expect(getByText(/9:30 AM/)).toBeTruthy();
    });

    it('should format date correctly for different dates', () => {
        const differentDateVisit = {
            ...mockVisit,
            visitDate: '2023-12-25',
        };

        const { getByText } = render(
            <VisitCard visit={differentDateVisit} />
        );

        expect(getByText(/Dec 25, 2023/)).toBeTruthy();
    });

    it('should handle visit without staff name', () => {
        const visitWithoutStaff = {
            ...mockVisit,
            staffName: '',
        };

        const { queryByText } = render(
            <VisitCard visit={visitWithoutStaff} />
        );

        expect(queryByText(/Jane Smith/)).toBeNull();
    });

    it('should not show edit button when canEdit is false', () => {
        const { queryByRole } = render(
            <VisitCard visit={mockVisit} canEdit={false} />
        );

        expect(queryByRole('button')).toBeNull();
    });

    it('should not show edit button when onEdit is not provided', () => {
        const { queryByRole } = render(
            <VisitCard visit={mockVisit} canEdit={true} />
        );

        expect(queryByRole('button')).toBeNull();
    });
});