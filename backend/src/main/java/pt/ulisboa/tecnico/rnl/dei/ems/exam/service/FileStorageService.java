package pt.ulisboa.tecnico.rnl.dei.ems.exam.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;

@Service
public class FileStorageService {

	@Value("${exam.upload.path:./uploads/exams}")
	private String examUploadPath;

	@Value("${question.upload.path:./uploads/questions}")
	private String questionUploadPath;

	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(Paths.get(examUploadPath));
			Files.createDirectories(Paths.get(questionUploadPath));
		} catch (IOException e) {
			throw new DEIException(ErrorMessage.FILE_STORAGE_FAILED, "Failed to create upload directories: " + e.getMessage());
		}
	}

	public String storeExamPdf(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new DEIException(ErrorMessage.EXAM_PDF_REQUIRED);
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
			throw new DEIException(ErrorMessage.EXAM_PDF_INVALID);
		}

		String uniqueFilename = UUID.randomUUID() + ".pdf";
		Path targetLocation = Paths.get(examUploadPath).resolve(uniqueFilename).normalize();

		try (InputStream inputStream = file.getInputStream()) {
			Files.createDirectories(targetLocation.getParent());
			Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return targetLocation.toString();
		} catch (IOException e) {
			throw new DEIException(ErrorMessage.FILE_STORAGE_FAILED, e.getMessage());
		}
	}

	public String storeQuestionImage(byte[] imageBytes, String extension) {
		String uniqueFilename = UUID.randomUUID() + "." + extension;
		Path targetLocation = Paths.get(questionUploadPath).resolve(uniqueFilename).normalize();

		try {
			Files.createDirectories(targetLocation.getParent());
			Files.write(targetLocation, imageBytes);
			return targetLocation.toString();
		} catch (IOException e) {
			throw new DEIException(ErrorMessage.FILE_STORAGE_FAILED, e.getMessage());
		}
	}

	public String storeAnnotatedQuestionImage(String base64Data) {
		if (base64Data == null || base64Data.isBlank()) {
			return null;
		}
		try {
			String cleanBase64 = base64Data;
			if (cleanBase64.contains(",")) {
				cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
			}
			byte[] imageBytes = java.util.Base64.getDecoder().decode(cleanBase64);
			return storeQuestionImage(imageBytes, "png");
		} catch (Exception e) {
			throw new DEIException(ErrorMessage.FILE_STORAGE_FAILED, "Failed to store annotated image: " + e.getMessage());
		}
	}

	public Resource loadFileAsResource(String filePath) {
		File file = new File(filePath);
		if (!file.exists() || !file.canRead()) {
			throw new DEIException(ErrorMessage.FILE_NOT_FOUND, filePath);
		}
		return new FileSystemResource(file);
	}

	public void deleteFileIfExists(String filePath) {
		if (filePath == null || filePath.isBlank()) {
			return;
		}
		try {
			Files.deleteIfExists(Paths.get(filePath));
		} catch (IOException ignored) {
		}
	}
}
