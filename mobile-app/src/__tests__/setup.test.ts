describe('Test Setup', () => {
  it('should run basic test', () => {
    expect(1 + 1).toBe(2);
  });

  it('should have mocked AsyncStorage', () => {
    const AsyncStorage = require('@react-native-async-storage/async-storage');
    expect(AsyncStorage.setItem).toBeDefined();
    expect(AsyncStorage.getItem).toBeDefined();
  });

  it('should have mocked React Navigation', () => {
    const { useNavigation } = require('@react-navigation/native');
    const navigation = useNavigation();
    expect(navigation.navigate).toBeDefined();
    expect(navigation.goBack).toBeDefined();
  });
});