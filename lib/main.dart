import 'package:flutter/material.dart';
import 'package:web_browser/web_browser.dart';
import 'package:speech_to_text/speech_recognition_result.dart';
import 'package:speech_to_text/speech_to_text.dart';

List<String> titles = <String>[
  'Домой',
  'Настройки',
];

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    BrowserController.globalStateExpiration = const Duration(days: 1);
    return MaterialApp(
      title: 'MajorDroid Minimal',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'MajorDroid Minimal'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> with TickerProviderStateMixin {
  final SpeechToText _speechToText = SpeechToText();
  bool _speechEnabled = false;
  String _lastWords = '';
  late final TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _tabController.addListener(updateIndex);
    _initSpeech();
  }

  void _initSpeech() async {
    _speechEnabled = await _speechToText.initialize();
    setState(() {});
  }

  void _startListening() async {
    await _speechToText.listen(onResult: _onSpeechResult);
    setState(() {});
  }

  void _stopListening() async {
    await _speechToText.stop();
    setState(() {});
  }

  void _onSpeechResult(SpeechRecognitionResult result) {
    setState(() {
      _lastWords = result.recognizedWords;
    });
  }

  void updateIndex() {
    setState(() {});
  }

  @override
  void dispose() {
    _tabController.removeListener(updateIndex);
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        toolbarHeight: 0,
        notificationPredicate: (ScrollNotification notification) {
          return notification.depth == 1;
        },
        scrolledUnderElevation: 4.0,
        shadowColor: Theme.of(context).shadowColor,
        bottom: TabBar(
          controller: _tabController,
          tabs: const <Widget>[
            Tab(
              icon: Icon(Icons.home),
              text: 'Домой',
            ),
            Tab(
              icon: Icon(Icons.settings),
              text: 'Настройки',
            ),
          ],
        ),
      ),
      body: TabBarView(
        physics: const NeverScrollableScrollPhysics(),
        controller: _tabController,
        children: <Widget>[
          Center(
            child: SafeArea(
              child: Column(
                children: <Widget>[
                  Text(
                    _speechToText.isListening
                        ? _lastWords
                        : _speechEnabled
                            ? 'Tap the microphone to start listening...'
                            : 'Speech not available',
                  ),
                  const Expanded(
                    child: Browser(
                      initialUriString: 'http://192.168.1.116/menu.html',
                      topBar: null,
                      bottomBar: null,
                    ),
                  ),
                ],
              ),
            ),
          ),
          Center(
            child: Text("Раздел с настройками"),
          ),
        ],
      ),
      floatingActionButton: _tabController.index == 0
          ? FloatingActionButton(
              onPressed: _speechToText.isNotListening
                  ? _startListening
                  : _stopListening,
              tooltip: 'Listen',
              child: Icon(
                  _speechToText.isNotListening ? Icons.mic_off : Icons.mic),
            )
          : Container(),
    );
  }
}
