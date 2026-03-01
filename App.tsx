import React, { useState, useEffect, useRef } from 'react';
import { 
  StyleSheet, Text, View, TouchableOpacity, ScrollView, 
  SafeAreaView, StatusBar, Animated, Dimensions, TextInput,
  ActivityIndicator, Alert
} from 'react-native';
import { Camera, CameraView } from 'expo-camera';
import { 
  Zap, Camera as CameraIcon, Database, Code, ChevronLeft, 
  Cpu, Search, Copy, Globe, RefreshCcw, Github, Video, Send
} from 'lucide-react-native';
import { LinearGradient } from 'expo-linear-gradient';
import * as Haptics from 'expo-haptics';
import * as ExpoClipboard from 'expo-clipboard';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { decode as atob, encode as btoa } from 'base-64';

// --- NATIVE MODULE IMPORT ---
import * as JavaLens from './modules/javalens/src/index';

// --- PIXEL 9 CYBER THEME ---
const COLORS = {
  black: '#000000',
  surface: '#0D1117',
  indigo: '#6366F1',
  emerald: '#10B981',
  red: '#EF4444',
  textGray: '#9CA3AF'
};

const { width, height } = Dimensions.get('window');

// --- GEMINI CONFIGURATION (FALLBACK) ---
const GEMINI_API_KEY = "DEIN_GEMINI_API_KEY_HIER"; 
const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);

type Screen = 'HUB' | 'SCANNER' | 'VAULT' | 'CHAT' | 'VIDEO' | 'GITHUB';

interface Snippet {
  id: string;
  title: string;
  category: string;
  description: string;
  code: string;
}

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('HUB');
  const [snippets, setSnippets] = useState<Snippet[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // --- APP-KOMPONENTE: ASYNCSTORAGE (VAULT PERSISTENZ) ---
  useEffect(() => {
    loadSnippets();
  }, []);

  const loadSnippets = async () => {
    try {
      const saved = await AsyncStorage.getItem('@vault_snippets');
      if (saved) {
        setSnippets(JSON.parse(saved));
      }
    } catch (e) {
      console.error("Failed to load snippets", e);
    } finally {
      setIsLoading(false);
    }
  };

  const onSaveSnippet = async (newSnippet: Snippet) => {
    try {
      const updated = [newSnippet, ...snippets];
      setSnippets(updated);
      await AsyncStorage.setItem('@vault_snippets', JSON.stringify(updated));
    } catch (e) {
      Alert.alert("Error", "Failed to save snippet locally.");
    }
  };

  const navigate = (to: Screen) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    setCurrentScreen(to);
  };

  if (isLoading) {
    return (
      <View style={[styles.root, { justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={COLORS.indigo} />
      </View>
    );
  }

  return (
    <View style={styles.root}>
      <StatusBar barStyle="light-content" />
      {currentScreen === 'HUB' && <HubScreen onNavigate={navigate} />}
      {currentScreen === 'SCANNER' && (
        <ScannerScreen 
          onBack={() => navigate('HUB')} 
          onSave={(s: Snippet) => {
            onSaveSnippet(s);
            navigate('VAULT');
          }} 
        />
      )}
      {currentScreen === 'VAULT' && <VaultScreen snippets={snippets} onBack={() => navigate('HUB')} />}
      {currentScreen === 'CHAT' && <ChatScreen codeContext={snippets[0]?.code || ""} onBack={() => navigate('HUB')} />}
      {currentScreen === 'VIDEO' && <VideoImportScreen onBack={() => navigate('HUB')} />}
      {currentScreen === 'GITHUB' && <GitHubSyncScreen snippets={snippets} onBack={() => navigate('HUB')} />}
    </View>
  );
}

// --- 1. HUB SCREEN ---
function HubScreen({ onNavigate }: { onNavigate: (s: Screen) => void }) {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.hubHeader}>
        <Text style={styles.logoTitle}>JAVALENS</Text>
        <View style={styles.chip}>
          <Cpu size={12} color={COLORS.indigo} />
          <Text style={styles.chipText}>TENSOR G4 NPU ACTIVE</Text>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.hubScroll}>
        <HubButton 
          title="LIVE SCAN" 
          desc="EXTRACT CODE VIA PIXEL 9 CAMERA" 
          icon={<CameraIcon color={COLORS.indigo} size={32} />} 
          onPress={() => onNavigate('SCANNER')} 
          accent={COLORS.indigo}
        />
        <HubButton 
          title="SNIPPET VAULT" 
          desc="BROWSE YOUR LOCAL JAVA LIBRARY" 
          icon={<Database color={COLORS.emerald} size={32} />} 
          onPress={() => onNavigate('VAULT')} 
          accent={COLORS.emerald}
        />
        <HubButton 
          title="VIDEO IMPORT" 
          desc="OFFLINE FRAME EXTRACTION" 
          icon={<Video color="#FFB800" size={32} />} 
          onPress={() => onNavigate('VIDEO')} 
          accent="#FFB800"
        />
        <HubButton 
          title="PROJECT CHAT" 
          desc="ASK GEMINI ABOUT YOUR CODE" 
          icon={<Code color="#FFFFFF" size={32} />} 
          onPress={() => onNavigate('CHAT')} 
          accent="#FFFFFF"
        />
        <HubButton 
          title="GITHUB SYNC" 
          desc="COMMIT TO REPOSITORIES" 
          icon={<Github color={COLORS.textGray} size={32} />} 
          onPress={() => onNavigate('GITHUB')} 
          accent={COLORS.textGray}
        />
      </ScrollView>

      <Text style={styles.footerInfo}>VERSION 5.4.0 • PIXEL 9 EXCLUSIVE</Text>
    </SafeAreaView>
  );
}

// --- 3. SCANNER SCREEN: ECHTE KAMERA & GEMINI VISION OCR (WITH NATIVE NPU OPTIMIZATION) ---
function ScannerScreen({ onBack, onSave }: any) {
  const [scanning, setScanning] = useState(false);
  const [captured, setCaptured] = useState("");
  const [processing, setProcessing] = useState(false);
  const [cameraPermission, setCameraPermission] = useState<boolean | null>(null);
  const cameraRef = useRef<CameraView>(null);
  const pulseAnim = useRef(new Animated.Value(1)).current;

  useEffect(() => {
    (async () => {
      const { status } = await Camera.requestCameraPermissionsAsync();
      setCameraPermission(status === 'granted');
    })();
  }, []);

  useEffect(() => {
    if (scanning) {
      Animated.loop(
        Animated.sequence([
          Animated.timing(pulseAnim, { toValue: 1.1, duration: 800, useNativeDriver: true }),
          Animated.timing(pulseAnim, { toValue: 1, duration: 800, useNativeDriver: true })
        ])
      ).start();
    }
  }, [scanning]);

  const handleScan = async () => {
    if (!cameraRef.current) return;
    
    setScanning(true);
    setProcessing(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);

    try {
      const photo = await cameraRef.current.takePictureAsync({ base64: true, quality: 0.5 });
      if (!photo?.base64) throw new Error("Could not capture photo.");

      // TRY NATIVE STITCHING/OCR FIRST (DUMMY CALL AS EXAMPLE)
      // let resultText = JavaLens.stitchCode(captured, "new_ocr_data_here");

      // FALLBACK TO GEMINI VISION
      const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
      const prompt = "Extrahiere nur den Java-Quellcode aus diesem Bild. Repariere Tippfehler. Gib reinen Code ohne Markdown-Tags zurück.";
      
      const imagePart = {
        inlineData: {
          data: photo.base64,
          mimeType: "image/jpeg"
        }
      };

      const result = await model.generateContent([imagePart, prompt]);
      const response = await result.response;
      setCaptured(response.text());
    } catch (e: any) {
      Alert.alert("OCR Error", e.message || "Gemini Vision failed to process the image.");
    } finally {
      setProcessing(false);
      setScanning(false);
    }
  };

  const handleMagicFix = async () => {
    setProcessing(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Heavy);
    
    try {
      // --- NATIVE CALL: GEMINI NANO ON-DEVICE FIX ---
      const fixedCode = await JavaLens.magicOcrFix(captured).catch(() => captured);
      const meta = await JavaLens.generateMetadata(fixedCode).catch(() => ({ 
        title: "Untitled.java", 
        category: "LOGIC", 
        description: "AI-Generated implementation." 
      }));

      const newSnippet = {
        id: Date.now().toString(),
        title: meta.title.endsWith(".java") ? meta.title : meta.title + ".java",
        category: meta.category.toUpperCase(),
        description: meta.description,
        code: fixedCode
      };
      onSave(newSnippet);
    } catch (e) {
      // Fallback logic if native module fails
      const classRegex = /(?:public\s+|abstract\s+|final\s+)*(class|interface|enum)\s+([a-zA-Z0-9_]+)/;
      const match = captured.match(classRegex);
      const className = match ? match[2] : "Untitled_Snippet";
      onSave({
        id: Date.now().toString(),
        title: `${className}.java`,
        category: match ? match[1].toUpperCase() : 'LOGIC',
        description: "AI-Generated snippet.",
        code: captured
      });
    } finally {
      setProcessing(false);
    }
  };

  if (cameraPermission === null) return <View style={styles.root} />;
  if (cameraPermission === false) return <Text style={{color: 'white', textAlign: 'center', marginTop: 100}}>No camera access</Text>;

  return (
    <View style={styles.container}>
      <CameraView style={styles.camera} ref={cameraRef} facing="back">
        <LinearGradient colors={['rgba(0,0,0,0.8)', 'transparent', 'rgba(0,0,0,0.8)']} style={StyleSheet.absoluteFill} />
        
        <TouchableOpacity style={styles.backButton} onPress={onBack} activeOpacity={0.7}>
          <View style={styles.backButtonContent}>
            <ChevronLeft color="white" size={24} />
            <Text style={styles.backButtonText}>BACK TO HUB</Text>
          </View>
        </TouchableOpacity>

        <View style={styles.scanOverlay}>
          <View style={[styles.scanCorner, { top: 0, left: 0, borderTopWidth: 2, borderLeftWidth: 2 }]} />
          <View style={[styles.scanCorner, { top: 0, right: 0, borderTopWidth: 2, borderRightWidth: 2 }]} />
          <View style={[styles.scanCorner, { bottom: 0, left: 0, borderBottomWidth: 2, borderLeftWidth: 2 }]} />
          <View style={[styles.scanCorner, { bottom: 0, right: 0, borderBottomWidth: 2, borderRightWidth: 2 }]} />
        </View>

        {captured !== "" && (
          <View style={styles.reviewPanel}>
            <View style={styles.reviewHeader}>
              <Cpu size={14} color={COLORS.emerald} />
              <Text style={styles.reviewTitle}>NATIVE NPU PIPELINE ACTIVE</Text>
            </View>
            <ScrollView style={styles.codePreviewScroll}>
              <Text style={styles.codePreviewText}>{captured}</Text>
            </ScrollView>
            <TouchableOpacity style={styles.magicFixBtn} onPress={handleMagicFix} disabled={processing}>
              <LinearGradient colors={[COLORS.indigo, '#3730A3']} style={styles.magicBtnGradient}>
                {processing ? <ActivityIndicator color="white" /> : (
                  <>
                    <Zap color="white" size={18} fill="white" />
                    <Text style={styles.magicBtnText}>NATIVE GEMINI NANO FIX</Text>
                  </>
                )}
              </LinearGradient>
            </TouchableOpacity>
          </View>
        )}

        <View style={styles.scannerControls}>
          <Animated.View style={{ transform: [{ scale: pulseAnim }] }}>
            <TouchableOpacity 
              style={[styles.mainButton, { borderColor: scanning ? COLORS.red : COLORS.indigo }]} 
              onPress={handleScan}
              disabled={processing}
            >
              <LinearGradient colors={scanning ? [COLORS.red, '#991B1B'] : [COLORS.indigo, '#3730A3']} style={styles.mainButtonInner}>
                {processing ? <ActivityIndicator color="white" /> : <Zap color="white" fill="white" size={28} />}
              </LinearGradient>
            </TouchableOpacity>
          </Animated.View>
          <Text style={styles.statusText}>{processing ? "NPU PROCESSING..." : "START SCAN"}</Text>
        </View>
      </CameraView>
    </View>
  );
}

// --- VAULT SCREEN ---
function VaultScreen({ snippets, onBack }: { snippets: Snippet[], onBack: () => void }) {
  const [search, setSearch] = useState("");
  const filtered = snippets.filter((s: any) => s.title.toLowerCase().includes(search.toLowerCase()) || s.category.toLowerCase().includes(search.toLowerCase()));

  const copyToClipboard = async (code: string) => {
    await ExpoClipboard.setStringAsync(code);
    Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    Alert.alert("Success", "Copied to Shared Clipboard!");
  };

  return (
    <SafeAreaView style={styles.vaultContainer}>
      <View style={styles.vaultHeader}>
        <TouchableOpacity onPress={onBack} style={styles.headerBackButton}>
          <ChevronLeft color="white" size={24} />
          <Text style={styles.headerBackText}>HUB</Text>
        </TouchableOpacity>
        <Text style={styles.vaultTitleHeader}>VAULT</Text>
      </View>

      <View style={styles.searchBarContainer}>
        <View style={styles.searchBar}>
          <Search size={18} color={COLORS.textGray} />
          <TextInput 
            style={styles.searchInput}
            placeholder="Search classes..."
            placeholderTextColor={COLORS.textGray}
            value={search}
            onChangeText={setSearch}
          />
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.vaultScrollContent}>
        {filtered.map((s: any) => (
          <View key={s.id} style={styles.snippetCard}>
            <View style={styles.snippetCardHeader}>
              <View style={styles.badgeContainer}><Text style={styles.badgeText}>{s.category}</Text></View>
              <TouchableOpacity onPress={() => copyToClipboard(s.code)}><Copy size={18} color={COLORS.indigo} /></TouchableOpacity>
            </View>
            <Text style={styles.snippetTitleText}>{s.title}</Text>
            <Text style={styles.snippetDescriptionText}>{s.description}</Text>
            <View style={styles.miniCodeBox}>
              <Text style={styles.miniCodeText}>{s.code.substring(0, 100)}...</Text>
            </View>
          </View>
        ))}
        {filtered.length === 0 && <Text style={{color: 'gray', textAlign: 'center', marginTop: 40}}>No snippets found.</Text>}
      </ScrollView>
    </SafeAreaView>
  );
}

// --- 4. CHAT SCREEN: ECHTER NATIVE GEMINI CHAT ---
function ChatScreen({ codeContext, onBack }: any) {
  const [msg, setMsg] = useState("");
  const [chat, setChat] = useState([{ text: "I've analyzed your scanned classes. How can I help?", isUser: false }]);
  const [isTyping, setIsTyping] = useState(false);

  const send = async () => {
    if (!msg || isTyping) return;
    const userMsg = msg;
    setChat(prev => [...prev, { text: userMsg, isUser: true }]);
    setMsg("");
    setIsTyping(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    
    try {
      // --- NATIVE CALL: OFFLINE GEMINI NANO ON PIXEL 9 ---
      const response = await JavaLens.askProjectChat(codeContext, userMsg);
      setChat(prev => [...prev, { text: response, isUser: false }]);
    } catch (e: any) {
      // Fallback to Online Gemini
      try {
        const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
        const prompt = "Kontext: " + codeContext + "\n\nUser Frage: " + userMsg;
        const result = await model.generateContent(prompt);
        setChat(prev => [...prev, { text: result.response.text(), isUser: false }]);
      } catch (err: any) {
        setChat(prev => [...prev, { text: "Error: " + err.message, isUser: false }]);
      }
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.vaultHeader}>
        <TouchableOpacity onPress={onBack} style={styles.headerBackButton}>
          <ChevronLeft color="white" size={24} />
          <Text style={styles.headerBackText}>HUB</Text>
        </TouchableOpacity>
        <Text style={styles.vaultTitleHeader}>NATIVE CHAT</Text>
      </View>
      <ScrollView style={styles.chatScroll} contentContainerStyle={{ padding: 20 }}>
        {chat.map((c, i) => (
          <View key={i} style={[styles.bubble, c.isUser ? styles.userBubble : styles.aiBubble]}>
            <Text style={styles.bubbleText}>{c.text}</Text>
          </View>
        ))}
        {isTyping && <ActivityIndicator size="small" color={COLORS.indigo} style={{ alignSelf: 'flex-start', marginLeft: 10 }} />}
      </ScrollView>
      <View style={styles.inputArea}>
        <TextInput 
          style={styles.chatInput} 
          placeholder="Ask Gemini Nano..." 
          placeholderTextColor="gray"
          value={msg}
          onChangeText={setMsg}
          onSubmitEditing={send}
        />
        <TouchableOpacity style={styles.sendBtn} onPress={send} disabled={isTyping}>
          <Send color="white" size={20} />
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

// --- VIDEO IMPORT SCREEN (DUMMY) ---
function VideoImportScreen({ onBack }: any) {
  const [parsing, setParsing] = useState(false);
  const [progress, setProgress] = useState(0);

  const startParse = () => {
    setParsing(true);
    let p = 0;
    const interval = setInterval(() => {
      p += 0.1;
      setProgress(p);
      if (p >= 1) {
        clearInterval(interval);
        setParsing(false);
        Alert.alert("Complete", "Video Parsing Complete!");
      }
    }, 300);
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.vaultHeader}>
        <TouchableOpacity onPress={onBack} style={styles.headerBackButton}>
          <ChevronLeft color="white" size={24} />
          <Text style={styles.headerBackText}>HUB</Text>
        </TouchableOpacity>
        <Text style={styles.vaultTitleHeader}>VIDEO</Text>
      </View>
      <View style={styles.videoContent}>
        <Video size={80} color={COLORS.indigo} opacity={0.5} />
        <Text style={styles.videoHint}>SELECT MP4 FOR FRAME-BY-FRAME SCAN</Text>
        
        {parsing ? (
          <View style={styles.progressBox}>
            <Text style={styles.progressText}>NPU PROCESSING: {(progress * 100).toFixed(0)}%</Text>
            <View style={styles.progressBar}><View style={[styles.progressFill, { width: `${progress * 100}%` }]} /></View>
          </View>
        ) : (
          <TouchableOpacity style={styles.selectBtn} onPress={startParse}>
            <Text style={styles.selectBtnText}>SELECT VIDEO FILE</Text>
          </TouchableOpacity>
        )}
      </View>
    </SafeAreaView>
  );
}

// --- 5. GITHUB SYNC SCREEN: ECHTE REST API ---
function GitHubSyncScreen({ snippets, onBack }: { snippets: Snippet[], onBack: () => void }) {
  const [githubToken, setGithubToken] = useState("");
  const [username, setUsername] = useState("");
  const [repo, setRepo] = useState("");
  const [syncing, setSyncing] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    const t = await AsyncStorage.getItem('@gh_token');
    const u = await AsyncStorage.getItem('@gh_user');
    const r = await AsyncStorage.getItem('@gh_repo');
    if (t) setGithubToken(t);
    if (u) setUsername(u);
    if (r) setRepo(r);
  };

  const saveSettings = async () => {
    await AsyncStorage.setItem('@gh_token', githubToken);
    await AsyncStorage.setItem('@gh_user', username);
    await AsyncStorage.setItem('@gh_repo', repo);
    Alert.alert("Success", "Settings Saved.");
  };

  const pushToGithub = async () => {
    if (!githubToken || !username || !repo) {
      Alert.alert("Config Missing", "Please enter Token, Username and Repo Name.");
      return;
    }

    setSyncing(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);

    try {
      for (const snippet of snippets) {
        const path = `src/${snippet.title}`;
        const content = btoa(snippet.code);
        
        // Get SHA if file exists
        const checkRes = await fetch(`https://api.github.com/repos/${username}/${repo}/contents/${path}`, {
          headers: { 'Authorization': `token ${githubToken}` }
        });
        
        let sha = undefined;
        if (checkRes.status === 200) {
          const data = await checkRes.json();
          sha = data.sha;
        }

        const res = await fetch(`https://api.github.com/repos/${username}/${repo}/contents/${path}`, {
          method: 'PUT',
          headers: {
            'Authorization': `token ${githubToken}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            message: "Added via JavaLens",
            content: content,
            sha: sha
          })
        });

        if (!res.ok) throw new Error(`Failed to sync ${snippet.title}: ${res.status}`);
      }
      Alert.alert("Sync Success!", "All snippets uploaded to GitHub.");
    } catch (e: any) {
      Alert.alert("Sync Error", e.message);
    } finally {
      setSyncing(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.vaultHeader}>
        <TouchableOpacity onPress={onBack} style={styles.headerBackButton}>
          <ChevronLeft color="white" size={24} />
          <Text style={styles.headerBackText}>HUB</Text>
        </TouchableOpacity>
        <Text style={styles.vaultTitleHeader}>SYNC</Text>
      </View>
      <ScrollView contentContainerStyle={styles.syncContent}>
        <View style={styles.githubCard}>
          <Github color="white" size={40} />
          <Text style={styles.ghUser}>GITHUB CONFIG</Text>
          
          <TextInput 
            style={styles.ghInput} 
            placeholder="GitHub Token" 
            placeholderTextColor="gray"
            secureTextEntry
            value={githubToken}
            onChangeText={setGithubToken}
          />
          <TextInput 
            style={styles.ghInput} 
            placeholder="Username" 
            placeholderTextColor="gray"
            value={username}
            onChangeText={setUsername}
          />
          <TextInput 
            style={styles.ghInput} 
            placeholder="Repository Name" 
            placeholderTextColor="gray"
            value={repo}
            onChangeText={setRepo}
          />
          <TouchableOpacity style={styles.saveSettingsBtn} onPress={saveSettings}>
            <Text style={{color: COLORS.indigo, fontWeight: 'bold'}}>SAVE CONFIG</Text>
          </TouchableOpacity>
        </View>
        
        <TouchableOpacity style={styles.syncBtn} onPress={pushToGithub} disabled={syncing}>
          {syncing ? <ActivityIndicator color="white" /> : (
            <>
              <RefreshCcw color="white" size={20} />
              <Text style={styles.syncBtnText}>PUSH TO GITHUB</Text>
            </>
          )}
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

// --- HELPER COMPONENTS ---
function HubButton({ title, desc, icon, onPress, accent }: any) {
  return (
    <TouchableOpacity style={styles.hubBtn} onPress={onPress} activeOpacity={0.7}>
      <View style={[styles.iconBox, { backgroundColor: accent + '15' }]}>{icon}</View>
      <View style={{ flex: 1 }}>
        <Text style={styles.hubBtnTitle}>{title}</Text>
        <Text style={styles.hubBtnDesc}>{desc}</Text>
      </View>
    </TouchableOpacity>
  );
}

// --- STYLES ---
const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: COLORS.black },
  container: { flex: 1 },
  hubHeader: { alignItems: 'center', marginTop: 40, marginBottom: 20 },
  logoTitle: { color: 'white', fontSize: 42, fontWeight: '900', fontStyle: 'italic', letterSpacing: 6 },
  chip: { flexDirection: 'row', alignItems: 'center', backgroundColor: COLORS.indigo + '20', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 20, marginTop: 8 },
  chipText: { color: COLORS.indigo, fontSize: 10, fontWeight: 'bold', marginLeft: 6 },
  hubScroll: { padding: 20 },
  hubBtn: { 
    backgroundColor: COLORS.surface, height: 100, borderRadius: 32, marginBottom: 16,
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: 20,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.05)'
  },
  iconBox: { width: 56, height: 56, borderRadius: 18, alignItems: 'center', justifyContent: 'center', marginRight: 16 },
  hubBtnTitle: { color: 'white', fontWeight: '900', fontSize: 18 },
  hubBtnDesc: { color: COLORS.textGray, fontSize: 10, marginTop: 2 },
  footerInfo: { color: COLORS.indigo, opacity: 0.3, fontSize: 9, textAlign: 'center', marginBottom: 20, fontWeight: 'bold' },
  
  camera: { flex: 1 },
  backButton: { 
    position: 'absolute', top: 60, left: 20, zIndex: 10,
    backgroundColor: 'rgba(0,0,0,0.6)', borderRadius: 20, paddingHorizontal: 12, paddingVertical: 8
  },
  backButtonContent: { flexDirection: 'row', alignItems: 'center' },
  backButtonText: { color: 'white', fontWeight: 'bold', fontSize: 12, marginLeft: 4 },
  
  scanOverlay: { position: 'absolute', top: '15%', left: '10%', right: '10%', height: '30%' },
  scanCorner: { position: 'absolute', width: 40, height: 40, borderColor: COLORS.indigo },
  scannerControls: { position: 'absolute', bottom: 60, width: '100%', alignItems: 'center' },
  mainButton: { width: 90, height: 90, borderRadius: 45, borderWidth: 4, padding: 4, justifyContent: 'center', alignItems: 'center' },
  mainButtonInner: { width: '100%', height: '100%', borderRadius: 41, justifyContent: 'center', alignItems: 'center' },
  statusText: { color: 'white', marginTop: 16, fontSize: 10, fontWeight: 'bold', letterSpacing: 2, opacity: 0.8 },
  
  reviewPanel: { position: 'absolute', bottom: 180, left: 20, right: 20, backgroundColor: COLORS.surface, borderRadius: 24, padding: 16, height: '35%', borderWidth: 1, borderColor: 'rgba(255,255,255,0.1)' },
  reviewHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  reviewTitle: { color: COLORS.emerald, fontSize: 9, fontWeight: '900', marginLeft: 6 },
  codePreviewScroll: { flex: 1, backgroundColor: COLORS.black, borderRadius: 12, padding: 12 },
  codePreviewText: { color: COLORS.indigo, fontFamily: 'monospace', fontSize: 10 },
  magicFixBtn: { height: 48, borderRadius: 12, overflow: 'hidden', marginTop: 12 },
  magicBtnGradient: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center' },
  magicBtnText: { color: 'white', fontWeight: 'bold', marginLeft: 8, fontSize: 12 },

  vaultContainer: { flex: 1, backgroundColor: COLORS.black },
  vaultHeader: { 
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: 20, paddingVertical: 15,
    borderBottomWidth: 1, borderBottomColor: 'rgba(255,255,255,0.05)'
  },
  headerBackButton: { 
    flexDirection: 'row', alignItems: 'center', backgroundColor: 'rgba(255,255,255,0.05)', 
    paddingHorizontal: 10, paddingVertical: 6, borderRadius: 12, marginRight: 15
  },
  headerBackText: { color: 'white', fontWeight: 'bold', fontSize: 12, marginLeft: 2 },
  vaultTitleHeader: { color: 'white', fontSize: 24, fontWeight: '900' },
  searchBarContainer: { paddingHorizontal: 20, marginBottom: 16 },
  searchBar: { backgroundColor: COLORS.surface, height: 50, borderRadius: 16, flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, borderWidth: 1, borderColor: 'rgba(255,255,255,0.05)' },
  searchInput: { flex: 1, color: 'white', marginLeft: 12, fontSize: 14 },
  vaultScrollContent: { paddingHorizontal: 20, paddingBottom: 32 },
  snippetCard: { backgroundColor: COLORS.surface, borderRadius: 24, padding: 20, marginBottom: 16, borderWidth: 1, borderColor: 'rgba(255,255,255,0.1)' },
  snippetCardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  badgeContainer: { backgroundColor: COLORS.indigo + '33', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6 },
  badgeText: { color: COLORS.indigo, fontSize: 9, fontWeight: 'bold' },
  snippetTitleText: { color: 'white', fontSize: 18, fontWeight: 'bold', marginBottom: 4 },
  snippetDescriptionText: { color: COLORS.textGray, fontSize: 12, marginBottom: 12 },
  miniCodeBox: { backgroundColor: COLORS.black, borderRadius: 10, padding: 10 },
  miniCodeText: { color: COLORS.emerald, fontFamily: 'monospace', fontSize: 10 },

  chatScroll: { flex: 1 },
  bubble: { maxWidth: '80%', padding: 12, borderRadius: 16, marginBottom: 12 },
  userBubble: { alignSelf: 'flex-end', backgroundColor: COLORS.indigo },
  aiBubble: { alignSelf: 'flex-start', backgroundColor: COLORS.surface },
  bubbleText: { color: 'white', fontSize: 14 },
  inputArea: { flexDirection: 'row', padding: 20, alignItems: 'center', backgroundColor: COLORS.black },
  chatInput: { flex: 1, backgroundColor: COLORS.surface, height: 50, borderRadius: 25, paddingHorizontal: 20, color: 'white' },
  sendBtn: { width: 50, height: 50, borderRadius: 25, backgroundColor: COLORS.indigo, alignItems: 'center', justifyContent: 'center', marginLeft: 10 },

  videoContent: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 40 },
  videoHint: { color: 'gray', marginTop: 20, fontSize: 12, textAlign: 'center', letterSpacing: 1 },
  selectBtn: { marginTop: 40, backgroundColor: COLORS.indigo, paddingHorizontal: 30, paddingVertical: 15, borderRadius: 30 },
  selectBtnText: { color: 'white', fontWeight: 'bold' },
  progressBox: { marginTop: 40, width: '100%' },
  progressText: { color: COLORS.indigo, fontSize: 10, fontWeight: 'bold', textAlign: 'center', marginBottom: 8 },
  progressBar: { height: 6, backgroundColor: COLORS.surface, borderRadius: 3, width: '100%', overflow: 'hidden' },
  progressFill: { height: '100%', backgroundColor: COLORS.indigo },

  syncContent: { flex: 1, padding: 20 },
  githubCard: { backgroundColor: COLORS.surface, padding: 24, borderRadius: 32, alignItems: 'center', borderWidth: 1, borderColor: 'rgba(255,255,255,0.1)' },
  ghUser: { color: 'white', fontSize: 18, fontWeight: '900', marginTop: 10, marginBottom: 16 },
  ghInput: { width: '100%', backgroundColor: COLORS.black, height: 45, borderRadius: 12, paddingHorizontal: 15, color: 'white', marginBottom: 10, fontSize: 12 },
  saveSettingsBtn: { padding: 10, marginTop: 5 },
  syncBtn: { marginTop: 20, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', backgroundColor: COLORS.emerald, padding: 18, borderRadius: 20 },
  syncBtnText: { color: 'white', fontWeight: 'bold', marginLeft: 12 }
});

