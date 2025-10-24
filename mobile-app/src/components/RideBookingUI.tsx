import React, { useState, useRef, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Animated,
  Dimensions,
  Alert,
  Modal,
  StatusBar
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialIcons';
import MapView, { Marker, PROVIDER_GOOGLE } from 'react-native-maps';
import { BlurView } from '@react-native-blur/blur';

const { width, height } = Dimensions.get('window');

// Modern Colors & Theme
const theme = {
  colors: {
    primary: '#6366F1',
    secondary: '#8B5CF6', 
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    dark: '#1F2937',
    light: '#F9FAFB',
    white: '#FFFFFF',
    gray: '#6B7280',
    background: '#F8FAFC'
  },
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32
  },
  borderRadius: {
    sm: 8,
    md: 12,
    lg: 16,
    xl: 20,
    full: 999
  }
};

/**
 * Modern Ride Booking Interface
 */
const RideBookingUI: React.FC = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [pickup, setPickup] = useState('');
  const [destination, setDestination] = useState('');
  const [rideType, setRideType] = useState('standard');
  const [showModal, setShowModal] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  
  const slideAnim = useRef(new Animated.Value(0)).current;
  const fadeAnim = useRef(new Animated.Value(1)).current;

  // Animation for step transitions
  useEffect(() => {
    Animated.spring(slideAnim, {
      toValue: (currentStep - 1) * -width,
      useNativeDriver: true,
      tension: 100,
      friction: 8
    }).start();
  }, [currentStep]);

  // Ride types data
  const rideTypes = [
    {
      id: 'economy',
      name: 'Economy',
      price: '₺25-35',
      time: '3-5 dk',
      icon: 'local-taxi',
      color: theme.colors.success,
      description: 'Ekonomik seçenek'
    },
    {
      id: 'standard',
      name: 'Standard',
      price: '₺35-45',
      time: '2-4 dk',
      icon: 'directions-car',
      color: theme.colors.primary,
      description: 'Konforlu yolculuk'
    },
    {
      id: 'premium',
      name: 'Premium',
      price: '₺55-75',
      time: '1-3 dk',
      icon: 'airline-seat-recline-extra',
      color: theme.colors.warning,
      description: 'Lüks araçlarla'
    },
    {
      id: 'xl',
      name: 'XL',
      price: '₺45-60',
      time: '4-6 dk',
      icon: 'airport-shuttle',
      color: theme.colors.secondary,
      description: '6 kişilik araçlar'
    }
  ];

  const handleBookRide = () => {
    if (!pickup.trim() || !destination.trim()) {
      Alert.alert('Eksik Bilgi', 'Lütfen kalkış ve varış noktalarını girin.');
      return;
    }
    
    setIsSearching(true);
    setShowModal(true);
    
    // Simulate driver search
    setTimeout(() => {
      setIsSearching(false);
      Alert.alert('Sürücü Bulundu! 🚗', 'Sürücünüz 3 dakika içinde yanınızda olacak.');
    }, 3000);
  };

  const renderLocationInput = () => (
    <View style={styles.locationContainer}>
      <View style={styles.inputGroup}>
        <View style={styles.locationDots}>
          <View style={[styles.dot, { backgroundColor: theme.colors.success }]} />
          <View style={styles.connectionLine} />
          <View style={[styles.dot, { backgroundColor: theme.colors.danger }]} />
        </View>
        
        <View style={styles.inputsContainer}>
          <View style={styles.inputWrapper}>
            <Icon name="my-location" size={20} color={theme.colors.success} style={styles.inputIcon} />
            <TextInput
              style={styles.locationInput}
              placeholder="Nereden..."
              value={pickup}
              onChangeText={setPickup}
              placeholderTextColor={theme.colors.gray}
            />
          </View>
          
          <View style={styles.inputWrapper}>
            <Icon name="place" size={20} color={theme.colors.danger} style={styles.inputIcon} />
            <TextInput
              style={styles.locationInput}
              placeholder="Nereye..."
              value={destination}
              onChangeText={setDestination}
              placeholderTextColor={theme.colors.gray}
            />
          </View>
        </View>
        
        <TouchableOpacity style={styles.swapButton}>
          <Icon name="swap-vert" size={24} color={theme.colors.primary} />
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderRideTypes = () => (
    <ScrollView style={styles.rideTypesContainer} showsVerticalScrollIndicator={false}>
      <Text style={styles.sectionTitle}>Araç Seçin</Text>
      {rideTypes.map((ride) => (
        <TouchableOpacity
          key={ride.id}
          style={[
            styles.rideTypeCard,
            rideType === ride.id && styles.selectedRideType
          ]}
          onPress={() => setRideType(ride.id)}
        >
          <LinearGradient
            colors={rideType === ride.id ? [ride.color, `${ride.color}CC`] : ['transparent', 'transparent']}
            style={styles.rideTypeGradient}
          >
            <View style={styles.rideTypeContent}>
              <View style={[styles.rideIcon, { backgroundColor: ride.color }]}>
                <Icon name={ride.icon} size={24} color={theme.colors.white} />
              </View>
              
              <View style={styles.rideInfo}>
                <Text style={[styles.rideName, rideType === ride.id && { color: theme.colors.white }]}>
                  {ride.name}
                </Text>
                <Text style={[styles.rideDescription, rideType === ride.id && { color: theme.colors.white, opacity: 0.9 }]}>
                  {ride.description}
                </Text>
              </View>
              
              <View style={styles.ridePrice}>
                <Text style={[styles.priceText, rideType === ride.id && { color: theme.colors.white }]}>
                  {ride.price}
                </Text>
                <Text style={[styles.timeText, rideType === ride.id && { color: theme.colors.white, opacity: 0.9 }]}>
                  {ride.time}
                </Text>
              </View>
            </View>
          </LinearGradient>
        </TouchableOpacity>
      ))}
    </ScrollView>
  );

  const renderMap = () => (
    <View style={styles.mapContainer}>
      <MapView
        provider={PROVIDER_GOOGLE}
        style={styles.map}
        initialRegion={{
          latitude: 41.0082,
          longitude: 28.9784,
          latitudeDelta: 0.01,
          longitudeDelta: 0.01,
        }}
      >
        <Marker
          coordinate={{ latitude: 41.0082, longitude: 28.9784 }}
          title="Kalkış Noktası"
        />
        <Marker
          coordinate={{ latitude: 41.0102, longitude: 28.9804 }}
          title="Varış Noktası"
        />
      </MapView>
      
      {/* Map Overlay */}
      <View style={styles.mapOverlay}>
        <BlurView style={styles.mapInfo} blurType="light" blurAmount={10}>
          <Text style={styles.mapInfoTitle}>Tahmini Süre</Text>
          <Text style={styles.mapInfoValue}>12 dakika</Text>
          <Text style={styles.mapInfoSubtitle}>2.3 km mesafe</Text>
        </BlurView>
      </View>
    </View>
  );

  const renderBookingButton = () => (
    <View style={styles.bookingButtonContainer}>
      <TouchableOpacity style={styles.bookingButton} onPress={handleBookRide}>
        <LinearGradient
          colors={[theme.colors.primary, theme.colors.secondary]}
          style={styles.bookingButtonGradient}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
        >
          <Icon name="local-taxi" size={24} color={theme.colors.white} />
          <Text style={styles.bookingButtonText}>Yolculuğu Başlat</Text>
        </LinearGradient>
      </TouchableOpacity>
    </View>
  );

  return (
    <View style={styles.container}>
      <StatusBar backgroundColor={theme.colors.primary} barStyle="light-content" />
      
      {/* Header */}
      <LinearGradient
        colors={[theme.colors.primary, theme.colors.secondary]}
        style={styles.header}
      >
        <Text style={styles.headerTitle}>Taksi Çağır 🚕</Text>
        <Text style={styles.headerSubtitle}>Hızlı ve güvenli ulaşım</Text>
      </LinearGradient>

      {/* Content Steps */}
      <Animated.View 
        style={[
          styles.stepsContainer,
          { transform: [{ translateX: slideAnim }] }
        ]}
      >
        {/* Step 1: Location Input */}
        <View style={[styles.step, { width }]}>
          <ScrollView showsVerticalScrollIndicator={false}>
            {renderLocationInput()}
            {renderRideTypes()}
          </ScrollView>
        </View>

        {/* Step 2: Map View */}
        <View style={[styles.step, { width }]}>
          {renderMap()}
        </View>
      </Animated.View>

      {/* Bottom Action */}
      {renderBookingButton()}

      {/* Steps Indicator */}
      <View style={styles.stepsIndicator}>
        {[1, 2].map((step) => (
          <TouchableOpacity
            key={step}
            style={[
              styles.stepDot,
              currentStep === step && styles.activeStepDot
            ]}
            onPress={() => setCurrentStep(step)}
          />
        ))}
      </View>

      {/* Driver Search Modal */}
      <Modal
        visible={showModal}
        transparent
        animationType="fade"
      >
        <BlurView style={styles.modalOverlay} blurType="dark" blurAmount={10}>
          <View style={styles.modalContent}>
            {isSearching ? (
              <>
                <Animated.View style={styles.searchingAnimation}>
                  <Icon name="search" size={48} color={theme.colors.primary} />
                </Animated.View>
                <Text style={styles.modalTitle}>Sürücü Aranıyor...</Text>
                <Text style={styles.modalSubtitle}>Size en yakın sürücüyü buluyoruz</Text>
              </>
            ) : (
              <>
                <Icon name="check-circle" size={48} color={theme.colors.success} />
                <Text style={styles.modalTitle}>Sürücü Bulundu!</Text>
                <Text style={styles.modalSubtitle}>Mehmet Bey - 3 dakika içinde yanınızda</Text>
                <TouchableOpacity
                  style={styles.modalButton}
                  onPress={() => setShowModal(false)}
                >
                  <Text style={styles.modalButtonText}>Tamam</Text>
                </TouchableOpacity>
              </>
            )}
          </View>
        </BlurView>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  
  // Header Styles
  header: {
    paddingTop: 50,
    paddingBottom: 30,
    paddingHorizontal: theme.spacing.lg,
    borderBottomLeftRadius: theme.borderRadius.xl,
    borderBottomRightRadius: theme.borderRadius.xl,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: theme.colors.white,
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 16,
    color: theme.colors.white,
    opacity: 0.9,
  },

  // Steps Container
  stepsContainer: {
    flex: 1,
    flexDirection: 'row',
  },
  step: {
    flex: 1,
    paddingHorizontal: theme.spacing.lg,
  },

  // Location Input Styles
  locationContainer: {
    marginTop: theme.spacing.lg,
    marginBottom: theme.spacing.xl,
  },
  inputGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.lg,
    padding: theme.spacing.md,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  locationDots: {
    alignItems: 'center',
    marginRight: theme.spacing.md,
  },
  dot: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  connectionLine: {
    width: 2,
    height: 30,
    backgroundColor: theme.colors.gray,
    opacity: 0.3,
    marginVertical: 4,
  },
  inputsContainer: {
    flex: 1,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: theme.spacing.sm,
  },
  inputIcon: {
    marginRight: theme.spacing.sm,
  },
  locationInput: {
    flex: 1,
    fontSize: 16,
    color: theme.colors.dark,
    paddingVertical: theme.spacing.sm,
  },
  swapButton: {
    padding: theme.spacing.sm,
    borderRadius: theme.borderRadius.md,
    backgroundColor: theme.colors.light,
  },

  // Ride Types Styles
  rideTypesContainer: {
    flex: 1,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.dark,
    marginBottom: theme.spacing.md,
  },
  rideTypeCard: {
    marginBottom: theme.spacing.sm,
    borderRadius: theme.borderRadius.lg,
    overflow: 'hidden',
    backgroundColor: theme.colors.white,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  selectedRideType: {
    elevation: 8,
    shadowOpacity: 0.2,
  },
  rideTypeGradient: {
    padding: theme.spacing.md,
  },
  rideTypeContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  rideIcon: {
    width: 48,
    height: 48,
    borderRadius: theme.borderRadius.md,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: theme.spacing.md,
  },
  rideInfo: {
    flex: 1,
  },
  rideName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: theme.colors.dark,
    marginBottom: 2,
  },
  rideDescription: {
    fontSize: 14,
    color: theme.colors.gray,
  },
  ridePrice: {
    alignItems: 'flex-end',
  },
  priceText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: theme.colors.dark,
  },
  timeText: {
    fontSize: 12,
    color: theme.colors.gray,
  },

  // Map Styles
  mapContainer: {
    flex: 1,
    marginTop: theme.spacing.lg,
    borderRadius: theme.borderRadius.lg,
    overflow: 'hidden',
  },
  map: {
    flex: 1,
  },
  mapOverlay: {
    position: 'absolute',
    top: theme.spacing.md,
    right: theme.spacing.md,
  },
  mapInfo: {
    padding: theme.spacing.md,
    borderRadius: theme.borderRadius.md,
    alignItems: 'center',
  },
  mapInfoTitle: {
    fontSize: 12,
    color: theme.colors.gray,
    marginBottom: 4,
  },
  mapInfoValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.dark,
  },
  mapInfoSubtitle: {
    fontSize: 12,
    color: theme.colors.gray,
  },

  // Booking Button Styles
  bookingButtonContainer: {
    padding: theme.spacing.lg,
    paddingBottom: theme.spacing.xl,
  },
  bookingButton: {
    borderRadius: theme.borderRadius.lg,
    shadowColor: theme.colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  bookingButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: theme.spacing.md,
    paddingHorizontal: theme.spacing.lg,
    borderRadius: theme.borderRadius.lg,
  },
  bookingButtonText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.white,
    marginLeft: theme.spacing.sm,
  },

  // Steps Indicator
  stepsIndicator: {
    flexDirection: 'row',
    justifyContent: 'center',
    paddingVertical: theme.spacing.md,
  },
  stepDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: theme.colors.gray,
    opacity: 0.3,
    marginHorizontal: 4,
  },
  activeStepDot: {
    backgroundColor: theme.colors.primary,
    opacity: 1,
  },

  // Modal Styles
  modalOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: theme.spacing.xl,
    alignItems: 'center',
    marginHorizontal: theme.spacing.lg,
    shadowColor: theme.colors.dark,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 16,
    elevation: 10,
  },
  searchingAnimation: {
    marginBottom: theme.spacing.lg,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: theme.colors.dark,
    marginBottom: theme.spacing.sm,
    textAlign: 'center',
  },
  modalSubtitle: {
    fontSize: 16,
    color: theme.colors.gray,
    textAlign: 'center',
    marginBottom: theme.spacing.lg,
  },
  modalButton: {
    backgroundColor: theme.colors.primary,
    paddingHorizontal: theme.spacing.xl,
    paddingVertical: theme.spacing.md,
    borderRadius: theme.borderRadius.lg,
  },
  modalButtonText: {
    color: theme.colors.white,
    fontSize: 16,
    fontWeight: 'bold',
  },
});

export default RideBookingUI;