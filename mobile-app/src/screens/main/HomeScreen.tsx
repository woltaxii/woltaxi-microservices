/**
 * WOLTAXI Home Screen
 * 
 * Ana sayfa - Harita, konum seçimi ve araç çağırma
 */

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  Dimensions,
  PermissionsAndroid,
  Platform,
  TextInput,
} from 'react-native';
import MapView, { Marker, Region } from 'react-native-maps';
import Geolocation from 'react-native-geolocation-service';
import Icon from 'react-native-vector-icons/MaterialIcons';
import LinearGradient from 'react-native-linear-gradient';

const { width, height } = Dimensions.get('window');

interface HomeScreenProps {
  navigation: any;
}

interface Location {
  latitude: number;
  longitude: number;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ navigation }) => {
  const [currentLocation, setCurrentLocation] = useState<Location | null>(null);
  const [pickupLocation, setPickupLocation] = useState<Location | null>(null);
  const [destinationLocation, setDestinationLocation] = useState<Location | null>(null);
  const [pickupAddress, setPickupAddress] = useState('');
  const [destinationAddress, setDestinationAddress] = useState('');
  const [isLoadingLocation, setIsLoadingLocation] = useState(true);
  const [mapRegion, setMapRegion] = useState<Region>({
    latitude: 41.0082,
    longitude: 28.9784,
    latitudeDelta: 0.01,
    longitudeDelta: 0.01,
  });

  useEffect(() => {
    getCurrentLocation();
  }, []);

  /**
   * Mevcut Konumu Al
   */
  const getCurrentLocation = async () => {
    try {
      if (Platform.OS === 'android') {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
        );
        if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
          Alert.alert('Konum İzni', 'Konum izni reddedildi');
          setIsLoadingLocation(false);
          return;
        }
      }

      Geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          const location = { latitude, longitude };
          
          setCurrentLocation(location);
          setPickupLocation(location);
          setMapRegion({
            ...mapRegion,
            latitude,
            longitude,
          });
          
          // Adres bilgisini al
          getAddressFromCoordinates(latitude, longitude, 'pickup');
          setIsLoadingLocation(false);
        },
        (error) => {
          console.error('Location error:', error);
          Alert.alert('Konum Hatası', 'Konum alınamadı');
          setIsLoadingLocation(false);
        },
        {
          enableHighAccuracy: true,
          timeout: 15000,
          maximumAge: 10000,
        }
      );
    } catch (error) {
      console.error('getCurrentLocation error:', error);
      setIsLoadingLocation(false);
    }
  };

  /**
   * Koordinatlardan Adres Al (Mock Implementation)
   */
  const getAddressFromCoordinates = async (
    latitude: number, 
    longitude: number, 
    type: 'pickup' | 'destination'
  ) => {
    try {
      // Bu gerçek bir uygulamada Google Maps API veya başka bir servis kullanılır
      const mockAddress = `Enlem: ${latitude.toFixed(4)}, Boylam: ${longitude.toFixed(4)}`;
      
      if (type === 'pickup') {
        setPickupAddress(mockAddress);
      } else {
        setDestinationAddress(mockAddress);
      }
    } catch (error) {
      console.error('Reverse geocoding error:', error);
    }
  };

  /**
   * Harita Üzerinde Tıklama
   */
  const handleMapPress = (event: any) => {
    const { latitude, longitude } = event.nativeEvent.coordinate;
    
    if (!destinationLocation) {
      // İlk tıklama - Varış noktası
      setDestinationLocation({ latitude, longitude });
      getAddressFromCoordinates(latitude, longitude, 'destination');
    } else {
      // İkinci tıklama - Varış noktasını güncelle
      setDestinationLocation({ latitude, longitude });
      getAddressFromCoordinates(latitude, longitude, 'destination');
    }
  };

  /**
   * Araç Çağır
   */
  const handleRequestRide = async () => {
    if (!pickupLocation || !destinationLocation) {
      Alert.alert('Hata', 'Lütfen başlangıç ve varış noktalarını seçin');
      return;
    }

    try {
      Alert.alert(
        'Araç Çağırılıyor',
        `Başlangıç: ${pickupAddress}\nVarış: ${destinationAddress}`,
        [
          { text: 'İptal', style: 'cancel' },
          { 
            text: 'Onayla', 
            onPress: () => {
              // API çağrısı burada yapılacak
              Alert.alert('Başarılı', 'Araç çağrıldı! Sürücü aranıyor...');
            }
          }
        ]
      );
    } catch (error) {
      console.error('Ride request error:', error);
      Alert.alert('Hata', 'Araç çağırma işlemi başarısız');
    }
  };

  /**
   * Konumları Temizle
   */
  const clearLocations = () => {
    setDestinationLocation(null);
    setDestinationAddress('');
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <LinearGradient
        colors={['#E30613', '#FF6B6B']}
        style={styles.header}
      >
        <View style={styles.headerContent}>
          <Text style={styles.headerTitle}>WOLTAXI</Text>
          <TouchableOpacity onPress={() => navigation.navigate('Profile')}>
            <Icon name="account-circle" size={32} color="white" />
          </TouchableOpacity>
        </View>
      </LinearGradient>

      {/* Map */}
      <View style={styles.mapContainer}>
        <MapView
          style={styles.map}
          region={mapRegion}
          onPress={handleMapPress}
          showsUserLocation={true}
          showsMyLocationButton={true}
        >
          {pickupLocation && (
            <Marker
              coordinate={pickupLocation}
              title="Başlangıç Noktası"
              description={pickupAddress}
              pinColor="#E30613"
            />
          )}
          {destinationLocation && (
            <Marker
              coordinate={destinationLocation}
              title="Varış Noktası"
              description={destinationAddress}
              pinColor="#FFD700"
            />
          )}
        </MapView>

        {/* Location Info Card */}
        <View style={styles.locationCard}>
          <View style={styles.locationRow}>
            <Icon name="my-location" size={20} color="#E30613" />
            <Text style={styles.locationText} numberOfLines={1}>
              {pickupAddress || 'Konumunuz alınıyor...'}
            </Text>
          </View>
          
          <View style={styles.divider} />
          
          <View style={styles.locationRow}>
            <Icon name="place" size={20} color="#FFD700" />
            <Text style={styles.locationText} numberOfLines={1}>
              {destinationAddress || 'Haritadan varış noktasını seçin'}
            </Text>
          </View>
        </View>
      </View>

      {/* Bottom Controls */}
      <View style={styles.bottomContainer}>
        <View style={styles.buttonRow}>
          <TouchableOpacity
            style={styles.clearButton}
            onPress={clearLocations}
          >
            <Icon name="clear" size={24} color="#666" />
            <Text style={styles.clearButtonText}>Temizle</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[
              styles.rideButton,
              (!destinationLocation) && styles.rideButtonDisabled
            ]}
            onPress={handleRequestRide}
            disabled={!destinationLocation}
          >
            <LinearGradient
              colors={['#FFD700', '#FFA500']}
              style={styles.rideButtonGradient}
            >
              <Icon name="local-taxi" size={24} color="#E30613" />
              <Text style={styles.rideButtonText}>ARAÇ ÇAĞIR</Text>
            </LinearGradient>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'white',
  },
  header: {
    paddingTop: Platform.OS === 'ios' ? 50 : 20,
    paddingBottom: 15,
    paddingHorizontal: 20,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    letterSpacing: 1,
  },
  mapContainer: {
    flex: 1,
    position: 'relative',
  },
  map: {
    flex: 1,
  },
  locationCard: {
    position: 'absolute',
    top: 20,
    left: 20,
    right: 20,
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 5,
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
  },
  locationText: {
    flex: 1,
    marginLeft: 10,
    fontSize: 14,
    color: '#333',
  },
  divider: {
    height: 1,
    backgroundColor: '#E0E0E0',
    marginVertical: 5,
  },
  bottomContainer: {
    backgroundColor: 'white',
    paddingHorizontal: 20,
    paddingVertical: 15,
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  clearButton: {
    flexDirection: 'column',
    alignItems: 'center',
    padding: 10,
  },
  clearButtonText: {
    fontSize: 12,
    color: '#666',
    marginTop: 4,
  },
  rideButton: {
    flex: 1,
    marginLeft: 20,
    borderRadius: 12,
    shadowColor: '#FFD700',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  rideButtonDisabled: {
    opacity: 0.5,
  },
  rideButtonGradient: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 16,
    borderRadius: 12,
  },
  rideButtonText: {
    color: '#E30613',
    fontSize: 16,
    fontWeight: 'bold',
    marginLeft: 8,
  },
});

export default HomeScreen;