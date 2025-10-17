import { useState, useEffect, useCallback, useRef } from 'react';
import { Dimensions } from 'react-native';

interface ViewportItem {
  index: number;
  isVisible: boolean;
}

interface UseViewportListProps {
  itemHeight: number;
  data: any[];
  overscan?: number; // Number of items to render outside viewport
}

export function useViewportList({ 
  itemHeight, 
  data, 
  overscan = 5 
}: UseViewportListProps) {
  const [visibleItems, setVisibleItems] = useState<Set<number>>(new Set());
  const [scrollOffset, setScrollOffset] = useState(0);
  const screenHeight = Dimensions.get('window').height;
  
  const updateVisibleItems = useCallback((offset: number) => {
    const startIndex = Math.max(0, Math.floor(offset / itemHeight) - overscan);
    const endIndex = Math.min(
      data.length - 1,
      Math.ceil((offset + screenHeight) / itemHeight) + overscan
    );
    
    const newVisibleItems = new Set<number>();
    for (let i = startIndex; i <= endIndex; i++) {
      newVisibleItems.add(i);
    }
    
    setVisibleItems(newVisibleItems);
  }, [itemHeight, data.length, screenHeight, overscan]);

  useEffect(() => {
    updateVisibleItems(scrollOffset);
  }, [updateVisibleItems, scrollOffset]);

  const onScroll = useCallback((event: any) => {
    const offset = event.nativeEvent.contentOffset.y;
    setScrollOffset(offset);
  }, []);

  const isItemVisible = useCallback((index: number) => {
    return visibleItems.has(index);
  }, [visibleItems]);

  return {
    onScroll,
    isItemVisible,
    visibleItems,
  };
}