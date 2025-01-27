public class ErrorImageComposite extends CompositeImageDescriptor {
    private Image mBaseImage;
    private ImageDescriptor mErrorImageDescriptor;
    private Point mSize;
    public ErrorImageComposite(Image baseImage) {
        mBaseImage = baseImage;
        mErrorImageDescriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_ERROR_TSK);
        mSize = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
    }
    @Override
    protected void drawCompositeImage(int width, int height) {
        ImageData baseData = mBaseImage.getImageData();
        drawImage(baseData, 0, 0);
        ImageData overlayData = mErrorImageDescriptor.getImageData();
        if (overlayData.width == baseData.width && baseData.height == baseData.height) {
            overlayData = overlayData.scaledTo(14, 14);
            drawImage(overlayData, -3, mSize.y - overlayData.height + 3);
        } else {
            drawImage(overlayData, 0, mSize.y - overlayData.height);
        }
    }
    @Override
    protected Point getSize() {
        return mSize;
    }
}
