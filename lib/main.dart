import 'package:flutter/material.dart';

void main() {
  runApp(const FlashGridApp());
}

class FlashGridApp extends StatelessWidget {
  const FlashGridApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: GridFlashScreen(),
    );
  }
}

class GridFlashScreen extends StatefulWidget {
  const GridFlashScreen({super.key});

  @override
  State<GridFlashScreen> createState() => _GridFlashScreenState();
}

class _GridFlashScreenState extends State<GridFlashScreen> {
  static const int rows = 6;
  static const int columns = 4;

  List<List<bool>> flashStates = List.generate(
    6,
    (_) => List.generate(4, (_) => false),
  );

  Future<void> _flashCell(int row, int col) async {
    for (int i = 0; i < 3; i++) {
      setState(() => flashStates[row][col] = true);
      await Future.delayed(const Duration(milliseconds: 100));
      setState(() => flashStates[row][col] = false);
      await Future.delayed(const Duration(milliseconds: 100));
    }
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final cellWidth = size.width / columns;
    final cellHeight = size.height / rows;

    return Scaffold(
      body: Column(
        children: List.generate(rows, (row) {
          return Row(
            children: List.generate(columns, (col) {
              return GestureDetector(
                onTap: () => _flashCell(row, col),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 100),
                  width: cellWidth,
                  height: cellHeight,
                  color: flashStates[row][col] ? Colors.white : Colors.black,
                ),
              );
            }),
          );
        }),
      ),
    );
  }
}
