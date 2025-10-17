import { NavigatorScreenParams } from '@react-navigation/native';

export type RootStackParamList = {
  Auth: undefined;
  Main: undefined;
};

export type AuthStackParamList = {
  Login: undefined;
};

export type MainTabParamList = {
  Guests: NavigatorScreenParams<GuestStackParamList>;
  Search: undefined;
  Notifications: undefined;
  Profile: undefined;
};

export type GuestStackParamList = {
  GuestList: undefined;
  GuestProfile: { guestId?: number };
  GuestDetail: { guestId: number };
  VisitHistory: { guestId: number };
  VisitLog: { guestId: number };
};