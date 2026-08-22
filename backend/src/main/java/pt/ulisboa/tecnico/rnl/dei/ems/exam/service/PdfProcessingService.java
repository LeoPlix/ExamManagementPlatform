package pt.ulisboa.tecnico.rnl.dei.ems.exam.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;

@Service
public class PdfProcessingService {

	private static final float RENDER_DPI = 150f;

	public int getPageCount(String pdfPath) {
		File file = new File(pdfPath);
		if (!file.exists()) {
			throw new DEIException(ErrorMessage.FILE_NOT_FOUND, pdfPath);
		}

		try (PDDocument document = Loader.loadPDF(file)) {
			return document.getNumberOfPages();
		} catch (IOException e) {
			throw new DEIException(ErrorMessage.PDF_PROCESSING_FAILED, e.getMessage());
		}
	}

	public byte[] renderPageToPng(String pdfPath, int pageNumber) {
		BufferedImage pageImage = renderPageImage(pdfPath, pageNumber);
		return bufferedImageToPngBytes(pageImage);
	}

	public byte[] cropPageRegionToPng(String pdfPath, int pageNumber,
			Double cropX, Double cropY, Double cropWidth, Double cropHeight) {

		BufferedImage fullPage = renderPageImage(pdfPath, pageNumber);

		// If no crop coordinates are supplied or width/height is zero, return full page
		if (cropX == null || cropY == null || cropWidth == null || cropHeight == null
				|| cropWidth <= 0 || cropHeight <= 0) {
			return bufferedImageToPngBytes(fullPage);
		}

		int imgWidth = fullPage.getWidth();
		int imgHeight = fullPage.getHeight();

		int x;
		int y;
		int w;
		int h;

		// Check if coordinates are normalized percentages (0.0 to 1.0)
		if (cropX <= 1.0 && cropY <= 1.0 && cropWidth <= 1.0 && cropHeight <= 1.0) {
			x = (int) Math.round(cropX * imgWidth);
			y = (int) Math.round(cropY * imgHeight);
			w = (int) Math.round(cropWidth * imgWidth);
			h = (int) Math.round(cropHeight * imgHeight);
		} else {
			x = (int) Math.round(cropX);
			y = (int) Math.round(cropY);
			w = (int) Math.round(cropWidth);
			h = (int) Math.round(cropHeight);
		}

		// Clamp bounding box strictly inside page dimensions
		x = Math.max(0, Math.min(x, imgWidth - 1));
		y = Math.max(0, Math.min(y, imgHeight - 1));
		w = Math.max(1, Math.min(w, imgWidth - x));
		h = Math.max(1, Math.min(h, imgHeight - y));

		BufferedImage cropped = fullPage.getSubimage(x, y, w, h);
		return bufferedImageToPngBytes(cropped);
	}

	private BufferedImage renderPageImage(String pdfPath, int pageNumber) {
		File file = new File(pdfPath);
		if (!file.exists()) {
			throw new DEIException(ErrorMessage.FILE_NOT_FOUND, pdfPath);
		}

		try (PDDocument document = Loader.loadPDF(file)) {
			int totalPages = document.getNumberOfPages();
			if (pageNumber < 1 || pageNumber > totalPages) {
				throw new DEIException(ErrorMessage.QUESTION_PAGE_INVALID);
			}

			PDFRenderer renderer = new PDFRenderer(document);
			// PDFRenderer page index is 0-based
			return renderer.renderImageWithDPI(pageNumber - 1, RENDER_DPI, ImageType.RGB);
		} catch (IOException e) {
			throw new DEIException(ErrorMessage.PDF_PROCESSING_FAILED, e.getMessage());
		}
	}

	private byte[] bufferedImageToPngBytes(BufferedImage image) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new DEIException(ErrorMessage.PDF_PROCESSING_FAILED, e.getMessage());
		}
	}
}
