public final class GestureUtils {
    private static final float SCALING_THRESHOLD = 0.26f;
    private static final float NONUNIFORM_SCALE = (float) Math.sqrt(2);
    private GestureUtils() {
    }
    static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close stream", e);
            }
        }
    }
    public static float[] spatialSampling(Gesture gesture, int bitmapSize) {
        return spatialSampling(gesture, bitmapSize, false);
    }
    public static float[] spatialSampling(Gesture gesture, int bitmapSize, 
            boolean keepAspectRatio) {
        final float targetPatchSize = bitmapSize - 1; 
        float[] sample = new float[bitmapSize * bitmapSize];
        Arrays.fill(sample, 0);
        RectF rect = gesture.getBoundingBox();
        final float gestureWidth = rect.width();
        final float gestureHeight = rect.height();
        float sx = targetPatchSize / gestureWidth;
        float sy = targetPatchSize / gestureHeight;
        if (keepAspectRatio) {
            float scale = sx < sy ? sx : sy;
            sx = scale;
            sy = scale;
        } else {
            float aspectRatio = gestureWidth / gestureHeight;
            if (aspectRatio > 1) {
                aspectRatio = 1 / aspectRatio;
            }
            if (aspectRatio < SCALING_THRESHOLD) {
                float scale = sx < sy ? sx : sy;
                sx = scale;
                sy = scale;
            } else {
                if (sx > sy) {
                    float scale = sy * NONUNIFORM_SCALE;
                    if (scale < sx) {
                        sx = scale;
                    }
                } else {
                    float scale = sx * NONUNIFORM_SCALE; 
                    if (scale < sy) {
                        sy = scale;
                    }
                }
            }
        }
        float preDx = -rect.centerX();
        float preDy = -rect.centerY();
        float postDx = targetPatchSize / 2;
        float postDy = targetPatchSize / 2;
        final ArrayList<GestureStroke> strokes = gesture.getStrokes();
        final int count = strokes.size();
        int size;
        float xpos;
        float ypos;
        for (int index = 0; index < count; index++) {
            final GestureStroke stroke = strokes.get(index);
            float[] strokepoints = stroke.points;
            size = strokepoints.length;
            final float[] pts = new float[size];
            for (int i = 0; i < size; i += 2) {
                pts[i] = (strokepoints[i] + preDx) * sx + postDx;
                pts[i + 1] = (strokepoints[i + 1] + preDy) * sy + postDy;
            }
            float segmentEndX = -1;
            float segmentEndY = -1;
            for (int i = 0; i < size; i += 2) {
                float segmentStartX = pts[i] < 0 ? 0 : pts[i];
                float segmentStartY = pts[i + 1] < 0 ? 0 : pts[i + 1];
                if (segmentStartX > targetPatchSize) {
                    segmentStartX = targetPatchSize;
                } 
                if (segmentStartY > targetPatchSize) {
                    segmentStartY = targetPatchSize;
                }
                plot(segmentStartX, segmentStartY, sample, bitmapSize);
                if (segmentEndX != -1) {
                    if (segmentEndX > segmentStartX) {
                        xpos = (float) Math.ceil(segmentStartX);
                        float slope = (segmentEndY - segmentStartY) / 
                                      (segmentEndX - segmentStartX);
                        while (xpos < segmentEndX) {
                            ypos = slope * (xpos - segmentStartX) + segmentStartY;
                            plot(xpos, ypos, sample, bitmapSize); 
                            xpos++;
                        }
                    } else if (segmentEndX < segmentStartX){
                        xpos = (float) Math.ceil(segmentEndX);
                        float slope = (segmentEndY - segmentStartY) / 
                                      (segmentEndX - segmentStartX);
                        while (xpos < segmentStartX) {
                            ypos = slope * (xpos - segmentStartX) + segmentStartY;
                            plot(xpos, ypos, sample, bitmapSize); 
                            xpos++;
                        }
                    }
                    if (segmentEndY > segmentStartY) {
                        ypos = (float) Math.ceil(segmentStartY);
                        float invertSlope = (segmentEndX - segmentStartX) / 
                                            (segmentEndY - segmentStartY);
                        while (ypos < segmentEndY) {
                            xpos = invertSlope * (ypos - segmentStartY) + segmentStartX;
                            plot(xpos, ypos, sample, bitmapSize); 
                            ypos++;
                        }
                    } else if (segmentEndY < segmentStartY) {
                        ypos = (float) Math.ceil(segmentEndY);
                        float invertSlope = (segmentEndX - segmentStartX) / 
                                            (segmentEndY - segmentStartY);
                        while (ypos < segmentStartY) {
                            xpos = invertSlope * (ypos - segmentStartY) + segmentStartX; 
                            plot(xpos, ypos, sample, bitmapSize); 
                            ypos++;
                        }
                    }
                } 
                segmentEndX = segmentStartX;
                segmentEndY = segmentStartY;
            }
        }
        return sample;
    }
    private static void plot(float x, float y, float[] sample, int sampleSize) {
        x = x < 0 ? 0 : x;
        y = y < 0 ? 0 : y;
        int xFloor = (int) Math.floor(x);
        int xCeiling = (int) Math.ceil(x);
        int yFloor = (int) Math.floor(y);
        int yCeiling = (int) Math.ceil(y);
        if (x == xFloor && y == yFloor) {
            int index = yCeiling * sampleSize + xCeiling;
            if (sample[index] < 1){
                sample[index] = 1;
            }
        } else {
            final double xFloorSq = Math.pow(xFloor - x, 2);
            final double yFloorSq = Math.pow(yFloor - y, 2);
            final double xCeilingSq = Math.pow(xCeiling - x, 2);
            final double yCeilingSq = Math.pow(yCeiling - y, 2);
            float topLeft = (float) Math.sqrt(xFloorSq + yFloorSq);
            float topRight = (float) Math.sqrt(xCeilingSq + yFloorSq);
            float btmLeft = (float) Math.sqrt(xFloorSq + yCeilingSq);
            float btmRight = (float) Math.sqrt(xCeilingSq + yCeilingSq);
            float sum = topLeft + topRight + btmLeft + btmRight;
            float value = topLeft / sum;
            int index = yFloor * sampleSize + xFloor;
            if (value > sample[index]){
                sample[index] = value;
            }
            value = topRight / sum;
            index = yFloor * sampleSize + xCeiling;
            if (value > sample[index]){
                sample[index] = value;
            }
            value = btmLeft / sum;
            index = yCeiling * sampleSize + xFloor;
            if (value > sample[index]){
                sample[index] = value;
            }
            value = btmRight / sum;
            index = yCeiling * sampleSize + xCeiling;
            if (value > sample[index]){
                sample[index] = value;
            }
        }
    }
    public static float[] temporalSampling(GestureStroke stroke, int numPoints) {
        final float increment = stroke.length / (numPoints - 1);
        int vectorLength = numPoints * 2;
        float[] vector = new float[vectorLength];
        float distanceSoFar = 0;
        float[] pts = stroke.points;
        float lstPointX = pts[0];
        float lstPointY = pts[1];
        int index = 0;
        float currentPointX = Float.MIN_VALUE;
        float currentPointY = Float.MIN_VALUE;
        vector[index] = lstPointX;
        index++;
        vector[index] = lstPointY;
        index++;
        int i = 0;
        int count = pts.length / 2;
        while (i < count) {
            if (currentPointX == Float.MIN_VALUE) {
                i++;
                if (i >= count) {
                    break;
                }
                currentPointX = pts[i * 2];
                currentPointY = pts[i * 2 + 1];
            }
            float deltaX = currentPointX - lstPointX;
            float deltaY = currentPointY - lstPointY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distanceSoFar + distance >= increment) {
                float ratio = (increment - distanceSoFar) / distance;
                float nx = lstPointX + ratio * deltaX;
                float ny = lstPointY + ratio * deltaY;
                vector[index] = nx;
                index++;
                vector[index] = ny;
                index++;
                lstPointX = nx;
                lstPointY = ny;
                distanceSoFar = 0;
            } else {
                lstPointX = currentPointX;
                lstPointY = currentPointY;
                currentPointX = Float.MIN_VALUE;
                currentPointY = Float.MIN_VALUE;
                distanceSoFar += distance;
            }
        }
        for (i = index; i < vectorLength; i += 2) {
            vector[i] = lstPointX;
            vector[i + 1] = lstPointY;
        }
        return vector;
    }
    static float[] computeCentroid(float[] points) {
        float centerX = 0;
        float centerY = 0;
        int count = points.length;
        for (int i = 0; i < count; i++) {
            centerX += points[i];
            i++;
            centerY += points[i];
        }
        float[] center = new float[2];
        center[0] = 2 * centerX / count;
        center[1] = 2 * centerY / count;
        return center;
    }
    private static float[][] computeCoVariance(float[] points) {
        float[][] array = new float[2][2];
        array[0][0] = 0;
        array[0][1] = 0;
        array[1][0] = 0;
        array[1][1] = 0;
        int count = points.length;
        for (int i = 0; i < count; i++) {
            float x = points[i];
            i++;
            float y = points[i];
            array[0][0] += x * x;
            array[0][1] += x * y;
            array[1][0] = array[0][1];
            array[1][1] += y * y;
        }
        array[0][0] /= (count / 2);
        array[0][1] /= (count / 2);
        array[1][0] /= (count / 2);
        array[1][1] /= (count / 2);
        return array;
    }
    static float computeTotalLength(float[] points) {
        float sum = 0;
        int count = points.length - 4;
        for (int i = 0; i < count; i += 2) {
            float dx = points[i + 2] - points[i];
            float dy = points[i + 3] - points[i + 1];
            sum += Math.sqrt(dx * dx + dy * dy);
        }
        return sum;
    }
    static float computeStraightness(float[] points) {
        float totalLen = computeTotalLength(points);
        float dx = points[2] - points[0];
        float dy = points[3] - points[1];
        return (float) Math.sqrt(dx * dx + dy * dy) / totalLen;
    }
    static float computeStraightness(float[] points, float totalLen) {
        float dx = points[2] - points[0];
        float dy = points[3] - points[1];
        return (float) Math.sqrt(dx * dx + dy * dy) / totalLen;
    }
    static float squaredEuclideanDistance(float[] vector1, float[] vector2) {
        float squaredDistance = 0;
        int size = vector1.length;
        for (int i = 0; i < size; i++) {
            float difference = vector1[i] - vector2[i];
            squaredDistance += difference * difference;
        }
        return squaredDistance / size;
    }
    static float cosineDistance(float[] vector1, float[] vector2) {
        float sum = 0;
        int len = vector1.length;
        for (int i = 0; i < len; i++) {
            sum += vector1[i] * vector2[i];
        }
        return (float) Math.acos(sum);
    }
    static float minimumCosineDistance(float[] vector1, float[] vector2, int numOrientations) {
        final int len = vector1.length;
        float a = 0;
        float b = 0;
        for (int i = 0; i < len; i += 2) {
            a += vector1[i] * vector2[i] + vector1[i + 1] * vector2[i + 1];
            b += vector1[i] * vector2[i + 1] - vector1[i + 1] * vector2[i];
        }
        if (a != 0) {
            final float tan = b/a;
            final double angle = Math.atan(tan);
            if (numOrientations > 2 && Math.abs(angle) >= Math.PI / numOrientations) {
                return (float) Math.acos(a);
            } else {
                final double cosine = Math.cos(angle);
                final double sine = cosine * tan; 
                return (float) Math.acos(a * cosine + b * sine);
            }
        } else {
            return (float) Math.PI / 2;
        }
    }
    public static OrientedBoundingBox computeOrientedBoundingBox(ArrayList<GesturePoint> originalPoints) {
        final int count = originalPoints.size();
        float[] points = new float[count * 2];
        for (int i = 0; i < count; i++) {
            GesturePoint point = originalPoints.get(i);
            int index = i * 2;
            points[index] = point.x;
            points[index + 1] = point.y;
        }
        float[] meanVector = computeCentroid(points);
        return computeOrientedBoundingBox(points, meanVector);
    }
    public static OrientedBoundingBox computeOrientedBoundingBox(float[] originalPoints) {
        int size = originalPoints.length;
        float[] points = new float[size];
        for (int i = 0; i < size; i++) {
            points[i] = originalPoints[i];
        }
        float[] meanVector = computeCentroid(points);
        return computeOrientedBoundingBox(points, meanVector);
    }
    private static OrientedBoundingBox computeOrientedBoundingBox(float[] points, float[] centroid) {
        translate(points, -centroid[0], -centroid[1]);
        float[][] array = computeCoVariance(points);
        float[] targetVector = computeOrientation(array);
        float angle;
        if (targetVector[0] == 0 && targetVector[1] == 0) {
            angle = (float) -Math.PI/2;
        } else { 
            angle = (float) Math.atan2(targetVector[1], targetVector[0]);
            rotate(points, -angle);
        }
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxx = Float.MIN_VALUE;
        float maxy = Float.MIN_VALUE;
        int count = points.length;
        for (int i = 0; i < count; i++) {
            if (points[i] < minx) {
                minx = points[i];
            }
            if (points[i] > maxx) {
                maxx = points[i];
            }
            i++;
            if (points[i] < miny) {
                miny = points[i];
            }
            if (points[i] > maxy) {
                maxy = points[i];
            }
        }
        return new OrientedBoundingBox((float) (angle * 180 / Math.PI), centroid[0], centroid[1], maxx - minx, maxy - miny);
    }
    private static float[] computeOrientation(float[][] covarianceMatrix) {
        float[] targetVector = new float[2];
        if (covarianceMatrix[0][1] == 0 || covarianceMatrix[1][0] == 0) {
            targetVector[0] = 1;
            targetVector[1] = 0;
        }
        float a = -covarianceMatrix[0][0] - covarianceMatrix[1][1];
        float b = covarianceMatrix[0][0] * covarianceMatrix[1][1] - covarianceMatrix[0][1]
                * covarianceMatrix[1][0];
        float value = a / 2;
        float rightside = (float) Math.sqrt(Math.pow(value, 2) - b);
        float lambda1 = -value + rightside;
        float lambda2 = -value - rightside;
        if (lambda1 == lambda2) {
            targetVector[0] = 0;
            targetVector[1] = 0;
        } else {
            float lambda = lambda1 > lambda2 ? lambda1 : lambda2;
            targetVector[0] = 1;
            targetVector[1] = (lambda - covarianceMatrix[0][0]) / covarianceMatrix[0][1];
        }
        return targetVector;
    }
    static float[] rotate(float[] points, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            float x = points[i] * cos - points[i + 1] * sin;
            float y = points[i] * sin + points[i + 1] * cos;
            points[i] = x;
            points[i + 1] = y;
        }
        return points;
    }
    static float[] translate(float[] points, float dx, float dy) {
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            points[i] += dx;
            points[i + 1] += dy;
        }
        return points;
    }
    static float[] scale(float[] points, float sx, float sy) {
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            points[i] *= sx;
            points[i + 1] *= sy;
        }
        return points;
    }
}
