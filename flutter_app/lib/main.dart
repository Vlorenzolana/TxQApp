import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('TxQApp Flutter')),
        body: GridView.count(
          crossAxisCount: 3,
          children: List.generate(9, (index) {
            return GestureDetector(
              onTap: () {
                debugPrint('Tapped on cell \$index');
              },
              child: Container(
                margin: const EdgeInsets.all(8),
                color: Colors.blueAccent,
              ),
            );
          }),
        ),
      ),
    );
  }
}
