/**
 * WOLTAXI Login Screen
 * 
 * Kullanıcı giriş ekranı - Telefon numarası ve şifre ile giriş
 */

import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Dimensions,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Icon from 'react-native-vector-icons/MaterialIcons';
import LinearGradient from 'react-native-linear-gradient';

const { width, height } = Dimensions.get('window');

interface LoginScreenProps {
  navigation: any;
}

const LoginScreen: React.FC<LoginScreenProps> = ({ navigation }) => {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  /**
   * Giriş İşlemi
   */
  const handleLogin = async () => {
    if (!phone.trim() || !password.trim()) {
      Alert.alert('Hata', 'Lütfen tüm alanları doldurun.');
      return;
    }

    if (phone.length < 10) {
      Alert.alert('Hata', 'Geçerli bir telefon numarası girin.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch('http://localhost:8765/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          phone: formatPhoneNumber(phone), 
          password 
        }),
      });

      const data = await response.json();

      if (response.ok) {
        // Token ve kullanıcı bilgilerini kaydet
        await AsyncStorage.setItem('authToken', data.token);
        await AsyncStorage.setItem('userData', JSON.stringify(data.user));
        
        Alert.alert('Başarılı', 'Giriş yapıldı!', [
          { text: 'Tamam', onPress: () => navigation.replace('MainTabs') }
        ]);
      } else {
        Alert.alert('Hata', data.message || 'Giriş başarısız!');
      }
    } catch (error) {
      console.error('Login error:', error);
      Alert.alert('Hata', 'Bağlantı hatası! Lütfen tekrar deneyin.');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Telefon Numarası Formatla
   */
  const formatPhoneNumber = (phoneNumber: string) => {
    const cleaned = phoneNumber.replace(/\D/g, '');
    if (cleaned.startsWith('90')) {
      return '+' + cleaned;
    }
    if (cleaned.startsWith('0')) {
      return '+90' + cleaned.substring(1);
    }
    return '+90' + cleaned;
  };

  /**
   * Telefon Numarası Input Formatı
   */
  const handlePhoneChange = (text: string) => {
    const cleaned = text.replace(/\D/g, '');
    if (cleaned.length <= 11) {
      setPhone(cleaned);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <LinearGradient
        colors={['#E30613', '#FF6B6B']}
        style={styles.header}
      >
        <View style={styles.logoContainer}>
          <View style={styles.logoCircle}>
            <Text style={styles.logoText}>W</Text>
          </View>
          <Text style={styles.brandName}>WOLTAXI</Text>
          <Text style={styles.welcomeText}>Hoş Geldiniz</Text>
        </View>
      </LinearGradient>

      <ScrollView style={styles.formContainer} showsVerticalScrollIndicator={false}>
        <View style={styles.formContent}>
          <Text style={styles.title}>Giriş Yapın</Text>
          <Text style={styles.subtitle}>
            Güvenli yolculuğunuz için giriş yapın
          </Text>

          {/* Telefon Numarası Input */}
          <View style={styles.inputContainer}>
            <Icon name="phone" size={20} color="#666" style={styles.inputIcon} />
            <Text style={styles.countryCode}>+90</Text>
            <TextInput
              style={styles.phoneInput}
              placeholder="5XX XXX XX XX"
              value={phone}
              onChangeText={handlePhoneChange}
              keyboardType="phone-pad"
              maxLength={11}
              placeholderTextColor="#999"
            />
          </View>

          {/* Şifre Input */}
          <View style={styles.inputContainer}>
            <Icon name="lock" size={20} color="#666" style={styles.inputIcon} />
            <TextInput
              style={styles.passwordInput}
              placeholder="Şifrenizi girin"
              value={password}
              onChangeText={setPassword}
              secureTextEntry={!showPassword}
              placeholderTextColor="#999"
            />
            <TouchableOpacity
              onPress={() => setShowPassword(!showPassword)}
              style={styles.eyeIcon}
            >
              <Icon 
                name={showPassword ? 'visibility' : 'visibility-off'} 
                size={20} 
                color="#666" 
              />
            </TouchableOpacity>
          </View>

          {/* Şifremi Unuttum */}
          <TouchableOpacity style={styles.forgotPasswordContainer}>
            <Text style={styles.forgotPasswordText}>Şifremi Unuttum</Text>
          </TouchableOpacity>

          {/* Giriş Butonu */}
          <TouchableOpacity
            style={[styles.loginButton, isLoading && styles.loginButtonDisabled]}
            onPress={handleLogin}
            disabled={isLoading}
          >
            <LinearGradient
              colors={['#E30613', '#FF6B6B']}
              style={styles.loginButtonGradient}
            >
              {isLoading ? (
                <Text style={styles.loginButtonText}>Giriş Yapılıyor...</Text>
              ) : (
                <>
                  <Icon name="login" size={20} color="white" />
                  <Text style={styles.loginButtonText}>GİRİŞ YAP</Text>
                </>
              )}
            </LinearGradient>
          </TouchableOpacity>

          {/* Kayıt Ol Linki */}
          <View style={styles.registerContainer}>
            <Text style={styles.registerText}>Hesabınız yok mu? </Text>
            <TouchableOpacity onPress={() => navigation.navigate('Register')}>
              <Text style={styles.registerLink}>Kayıt Olun</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  header: {
    height: height * 0.4,
    justifyContent: 'center',
    alignItems: 'center',
    borderBottomLeftRadius: 30,
    borderBottomRightRadius: 30,
  },
  logoContainer: {
    alignItems: 'center',
  },
  logoCircle: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: 'white',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 10,
  },
  logoText: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#E30613',
  },
  brandName: {
    fontSize: 32,
    fontWeight: 'bold',
    color: 'white',
    letterSpacing: 2,
    marginBottom: 5,
  },
  welcomeText: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.9)',
    fontWeight: '300',
  },
  formContainer: {
    flex: 1,
    marginTop: -30,
  },
  formContent: {
    backgroundColor: 'white',
    borderTopLeftRadius: 30,
    borderTopRightRadius: 30,
    paddingHorizontal: 30,
    paddingTop: 40,
    paddingBottom: 20,
    minHeight: height * 0.6,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    marginBottom: 30,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#e0e0e0',
    borderRadius: 12,
    marginBottom: 20,
    backgroundColor: '#f8f9fa',
    paddingHorizontal: 15,
    height: 55,
  },
  inputIcon: {
    marginRight: 10,
  },
  countryCode: {
    fontSize: 16,
    color: '#333',
    marginRight: 5,
    fontWeight: '500',
  },
  phoneInput: {
    flex: 1,
    fontSize: 16,
    color: '#333',
    paddingVertical: 0,
  },
  passwordInput: {
    flex: 1,
    fontSize: 16,
    color: '#333',
    paddingVertical: 0,
  },
  eyeIcon: {
    padding: 5,
  },
  forgotPasswordContainer: {
    alignItems: 'flex-end',
    marginBottom: 30,
  },
  forgotPasswordText: {
    color: '#E30613',
    fontSize: 14,
    fontWeight: '500',
  },
  loginButton: {
    borderRadius: 12,
    marginBottom: 30,
    shadowColor: '#E30613',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  loginButtonDisabled: {
    opacity: 0.7,
  },
  loginButtonGradient: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 16,
    borderRadius: 12,
  },
  loginButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
    marginLeft: 8,
  },
  registerContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  registerText: {
    color: '#666',
    fontSize: 14,
  },
  registerLink: {
    color: '#E30613',
    fontSize: 14,
    fontWeight: 'bold',
  },
});

export default LoginScreen;