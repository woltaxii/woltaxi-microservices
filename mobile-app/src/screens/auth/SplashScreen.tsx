/**
 * WOLTAXI Splash Screen
 * 
 * Uygulama açılış ekranı - Logo ve yükleme animasyonu
 */

import React, { useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Dimensions,
  StatusBar,
  ActivityIndicator,
} from 'react-native';
import * as Animatable from 'react-native-animatable';
import LinearGradient from 'react-native-linear-gradient';

const { width, height } = Dimensions.get('window');

const SplashScreen: React.FC = () => {
  
  return (
    <LinearGradient
      colors={['#E30613', '#FF6B6B', '#FFD700']}
      style={styles.container}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
    >
      <StatusBar barStyle="light-content" backgroundColor="#E30613" />
      
      {/* Logo Section */}
      <View style={styles.logoContainer}>
        <Animatable.View
          animation="bounceIn"
          duration={2000}
          style={styles.logoCircle}
        >
          <Text style={styles.logoText}>W</Text>
        </Animatable.View>
        
        <Animatable.Text
          animation="fadeInUp"
          delay={1000}
          duration={1500}
          style={styles.brandName}
        >
          WOLTAXI
        </Animatable.Text>
        
        <Animatable.Text
          animation="fadeInUp"
          delay={1500}
          duration={1000}
          style={styles.tagline}
        >
          Güvenilir Yolculuk Deneyimi
        </Animatable.Text>
      </View>

      {/* Loading Section */}
      <Animatable.View
        animation="fadeIn"
        delay={2000}
        style={styles.loadingContainer}
      >
        <ActivityIndicator size="large" color="white" />
        <Text style={styles.loadingText}>Yükleniyor...</Text>
      </Animatable.View>

      {/* Footer */}
      <Animatable.View
        animation="fadeInUp"
        delay={2500}
        style={styles.footer}
      >
        <Text style={styles.footerText}>
          v2.0.0 | WOLTAXI © 2024
        </Text>
        <Text style={styles.footerSubtext}>
          Türkiye'nin En Güvenilir Taksi Uygulaması
        </Text>
      </Animatable.View>
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: height * 0.15,
  },
  logoCircle: {
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 30,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 8,
    },
    shadowOpacity: 0.3,
    shadowRadius: 10,
    elevation: 15,
  },
  logoText: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#E30613',
  },
  brandName: {
    fontSize: 42,
    fontWeight: 'bold',
    color: 'white',
    letterSpacing: 4,
    marginBottom: 10,
    textShadowColor: 'rgba(0, 0, 0, 0.3)',
    textShadowOffset: { width: 2, height: 2 },
    textShadowRadius: 5,
  },
  tagline: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.9)',
    textAlign: 'center',
    fontWeight: '300',
    letterSpacing: 1,
  },
  loadingContainer: {
    alignItems: 'center',
    marginBottom: height * 0.1,
  },
  loadingText: {
    color: 'white',
    fontSize: 16,
    marginTop: 15,
    fontWeight: '300',
  },
  footer: {
    position: 'absolute',
    bottom: 50,
    alignItems: 'center',
  },
  footerText: {
    color: 'rgba(255, 255, 255, 0.8)',
    fontSize: 12,
    marginBottom: 5,
  },
  footerSubtext: {
    color: 'rgba(255, 255, 255, 0.6)',
    fontSize: 10,
    textAlign: 'center',
  },
});

export default SplashScreen;