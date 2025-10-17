import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import GuestListScreen from '../screens/guests/GuestListScreen';
import GuestProfileScreen from '../screens/guests/GuestProfileScreen';
import GuestDetailScreen from '../screens/guests/GuestDetailScreen';
import VisitHistoryScreen from '../screens/visits/VisitHistoryScreen';
import VisitLogScreen from '../screens/visits/VisitLogScreen';
import { GuestStackParamList } from '../types/navigation';

const Stack = createStackNavigator<GuestStackParamList>();

export default function GuestNavigator() {
  return (
    <Stack.Navigator>
      <Stack.Screen 
        name="GuestList" 
        component={GuestListScreen}
        options={{ title: 'Guests' }}
      />
      <Stack.Screen 
        name="GuestProfile" 
        component={GuestProfileScreen}
        options={{ headerShown: false }}
      />
      <Stack.Screen 
        name="GuestDetail" 
        component={GuestDetailScreen}
        options={{ title: 'Guest Details' }}
      />
      <Stack.Screen 
        name="VisitHistory" 
        component={VisitHistoryScreen}
        options={{ title: 'Visit History' }}
      />
      <Stack.Screen 
        name="VisitLog" 
        component={VisitLogScreen}
        options={{ title: 'Log Visit' }}
      />
    </Stack.Navigator>
  );
}