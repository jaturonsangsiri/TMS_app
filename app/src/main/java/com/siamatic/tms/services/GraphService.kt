import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.siamatic.tms.models.DataPoint

@Composable
fun SmoothLineChart(data: List<DataPoint>, modifier: Modifier = Modifier, showGrid: Boolean = true, lineColor: Color = Color.Red, smoothness: Float = 0.3f) {
  if (data.isEmpty()) return

  val minX = data.minOf { it.x }
  val maxX = data.maxOf { it.x }
  val minY = data.minOf { it.y }
  val maxY = data.maxOf { it.y }

  Canvas(modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val padding = 40.dp.toPx()

    // วาด Grid
    if (showGrid) {
      drawGrid(canvasWidth = canvasWidth, canvasHeight = canvasHeight, padding = padding)
    }

    // คำนวณตำแหน่งจุดกราฟ
    val points = data.map { point ->
      val x = padding + (point.x - minX) / (maxX - minX) * (canvasWidth - 2 * padding)
      val y = canvasHeight - padding - (point.y - minY) / (maxY - minY) * (canvasHeight - 2 * padding)
      Offset(x, y)
    }

    // วาดเส้นโค้งแบบนุ่มนวล
    drawSmoothLineChart(points, lineColor, smoothness)

    // วาดจุดข้อมูล
    drawDataPoints(points, lineColor)

    // วาดป้ายกำกับแกน
    drawAxisLabels(canvasWidth = canvasWidth, canvasHeight = canvasHeight, padding = padding)
  }
}

fun DrawScope.drawSmoothLineChart(points: List<Offset>, lineColor: Color, smoothness: Float = 0.3f) {
  if (points.size < 2) return

  val path = Path().apply {
    moveTo(points[0].x, points[0].y)

    for (i in 1 until points.size) {
      val currentPoint = points[i]
      val previousPoint = points[i - 1]

      // คำนวณจุดควบคุมสำหรับ Catmull-Rom spline
      val prevPrevPoint = if (i > 1) points[i - 2] else previousPoint
      val nextPoint = if (i < points.size - 1) points[i + 1] else currentPoint

      // คำนวณจุดควบคุม
      val controlPoint1X = previousPoint.x + (currentPoint.x - prevPrevPoint.x) * smoothness
      val controlPoint1Y = previousPoint.y + (currentPoint.y - prevPrevPoint.y) * smoothness

      val controlPoint2X = currentPoint.x - (nextPoint.x - previousPoint.x) * smoothness
      val controlPoint2Y = currentPoint.y - (nextPoint.y - previousPoint.y) * smoothness

      // วาดเส้นโค้งด้วย cubic bezier
      cubicTo(
        controlPoint1X, controlPoint1Y,
        controlPoint2X, controlPoint2Y,
        currentPoint.x, currentPoint.y
      )
    }
  }

  drawPath(
    path = path,
    color = lineColor,
    style = Stroke(
      width = 3.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )
}

fun DrawScope.drawGrid(canvasWidth: Float, canvasHeight: Float, padding: Float) {
  val gridColor = Color.Gray.copy(alpha = 0.3f)

  // วาดเส้นแนวนอน
  for (i in 0..10) {
    val y = padding + i * (canvasHeight - 2 * padding) / 10
    drawLine(
      color = gridColor,
      start = Offset(padding, y),
      end = Offset(canvasWidth - padding, y),
      strokeWidth = 1.dp.toPx()
    )
  }

  // วาดเส้นแนวตั้ง
  for (i in 0..10) {
    val x = padding + i * (canvasWidth - 2 * padding) / 10
    drawLine(
      color = gridColor,
      start = Offset(x, padding),
      end = Offset(x, canvasHeight - padding),
      strokeWidth = 1.dp.toPx()
    )
  }
}

fun DrawScope.drawDataPoints(points: List<Offset>, color: Color) {
  points.forEach { point ->
    drawCircle(
      color = color,
      radius = 6.dp.toPx(),
      center = point
    )
    drawCircle(
      color = Color.White,
      radius = 3.dp.toPx(),
      center = point
    )
  }
}

fun DrawScope.drawAxisLabels(canvasWidth: Float, canvasHeight: Float, padding: Float) {
  // วาดแกน X และ Y
  drawLine(
    color = Color.White,
    start = Offset(padding, canvasHeight - padding),
    end = Offset(canvasWidth - padding, canvasHeight - padding),
    strokeWidth = 2.dp.toPx()
  )

  drawLine(
    color = Color.White,
    start = Offset(padding, padding),
    end = Offset(padding, canvasHeight - padding),
    strokeWidth = 2.dp.toPx()
  )
}