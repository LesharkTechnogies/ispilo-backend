# ID Verification System - Complete Implementation Guide

I'll create a comprehensive system for ID verification with name and phone number validation, including admin verification workflow and OCR scanning.

## 🏗️ System Architecture

```
User Upload → OCR Processing → Admin Review → Verification Status
     ↓            ↓                ↓              ↓
  Mobile App   AI/OCR API    Admin Dashboard   Database
```

---

## 📱 Part 1: Flutter Mobile App (User Side)

### 1. **ID Upload Widget**

```dart
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:http/http.dart' as http;
import 'dart:io';
import 'dart:convert';

class IDVerificationPage extends StatefulWidget {
  @override
  _IDVerificationPageState createState() => _IDVerificationPageState();
}

class _IDVerificationPageState extends State<IDVerificationPage> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _idNumberController = TextEditingController();
  
  File? _frontIdImage;
  File? _backIdImage;
  File? _selfieImage;
  
  bool _isUploading = false;
  String _verificationStatus = 'Not Verified';
  
  final ImagePicker _picker = ImagePicker();

  // Pick image from camera or gallery
  Future<void> _pickImage(String type) async {
    final source = await showDialog<ImageSource>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Select Source'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: Icon(Icons.camera_alt),
              title: Text('Camera'),
              onTap: () => Navigator.pop(context, ImageSource.camera),
            ),
            ListTile(
              leading: Icon(Icons.photo_library),
              title: Text('Gallery'),
              onTap: () => Navigator.pop(context, ImageSource.gallery),
            ),
          ],
        ),
      ),
    );

    if (source == null) return;

    final XFile? image = await _picker.pickImage(
      source: source,
      maxWidth: 1920,
      maxHeight: 1080,
      imageQuality: 85,
    );

    if (image != null) {
      setState(() {
        switch (type) {
          case 'front':
            _frontIdImage = File(image.path);
            break;
          case 'back':
            _backIdImage = File(image.path);
            break;
          case 'selfie':
            _selfieImage = File(image.path);
            break;
        }
      });
    }
  }

  // Submit verification request
  Future<void> _submitVerification() async {
    if (!_formKey.currentState!.validate()) return;
    
    if (_frontIdImage == null || _selfieImage == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Please upload required documents')),
      );
      return;
    }

    setState(() => _isUploading = true);

    try {
      final request = http.MultipartRequest(
        'POST',
        Uri.parse('https://your-api.com/api/verification/submit'),
      );

      // Add authorization token
      final token = await _getAuthToken();
      request.headers['Authorization'] = 'Bearer $token';

      // Add form fields
      request.fields['name'] = _nameController.text;
      request.fields['phone'] = _phoneController.text;
      request.fields['idNumber'] = _idNumberController.text;

      // Add images
      request.files.add(
        await http.MultipartFile.fromPath('frontId', _frontIdImage!.path),
      );
      
      if (_backIdImage != null) {
        request.files.add(
          await http.MultipartFile.fromPath('backId', _backIdImage!.path),
        );
      }
      
      request.files.add(
        await http.MultipartFile.fromPath('selfie', _selfieImage!.path),
      );

      final response = await request.send();
      final responseData = await response.stream.bytesToString();
      final data = json.decode(responseData);

      if (response.statusCode == 200) {
        setState(() {
          _verificationStatus = 'Pending Review';
        });
        
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Verification submitted successfully!'),
            backgroundColor: Colors.green,
          ),
        );
      } else {
        throw Exception(data['message'] ?? 'Upload failed');
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() => _isUploading = false);
    }
  }

  Future<String> _getAuthToken() async {
    // Get from secure storage
    return 'your_jwt_token';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('ID Verification')),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Status Card
              Card(
                color: _getStatusColor(),
                child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Icon(_getStatusIcon(), color: Colors.white, size: 32),
                      SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Verification Status',
                              style: TextStyle(
                                color: Colors.white70,
                                fontSize: 12,
                              ),
                            ),
                            Text(
                              _verificationStatus,
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              
              SizedBox(height: 24),
              
              // Personal Information
              Text(
                'Personal Information',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              SizedBox(height: 12),
              
              TextFormField(
                controller: _nameController,
                decoration: InputDecoration(
                  labelText: 'Full Name',
                  border: OutlineInputBorder(),
                  prefixIcon: Icon(Icons.person),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter your name';
                  }
                  return null;
                },
              ),
              
              SizedBox(height: 16),
              
              TextFormField(
                controller: _phoneController,
                decoration: InputDecoration(
                  labelText: 'Phone Number',
                  border: OutlineInputBorder(),
                  prefixIcon: Icon(Icons.phone),
                ),
                keyboardType: TextInputType.phone,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter your phone number';
                  }
                  if (!RegExp(r'^\+?[0-9]{10,15}$').hasMatch(value)) {
                    return 'Please enter a valid phone number';
                  }
                  return null;
                },
              ),
              
              SizedBox(height: 16),
              
              TextFormField(
                controller: _idNumberController,
                decoration: InputDecoration(
                  labelText: 'ID/Passport Number',
                  border: OutlineInputBorder(),
                  prefixIcon: Icon(Icons.badge),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter your ID number';
                  }
                  return null;
                },
              ),
              
              SizedBox(height: 24),
              
              // Document Upload Section
              Text(
                'Upload Documents',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              SizedBox(height: 12),
              
              _buildImageUploadCard(
                title: 'Front of ID *',
                image: _frontIdImage,
                onTap: () => _pickImage('front'),
              ),
              
              SizedBox(height: 12),
              
              _buildImageUploadCard(
                title: 'Back of ID (Optional)',
                image: _backIdImage,
                onTap: () => _pickImage('back'),
              ),
              
              SizedBox(height: 12),
              
              _buildImageUploadCard(
                title: 'Selfie with ID *',
                image: _selfieImage,
                onTap: () => _pickImage('selfie'),
              ),
              
              SizedBox(height: 24),
              
              // Submit Button
              ElevatedButton(
                onPressed: _isUploading ? null : _submitVerification,
                style: ElevatedButton.styleFrom(
                  padding: EdgeInsets.symmetric(vertical: 16),
                  backgroundColor: Colors.blue,
                ),
                child: _isUploading
                    ? CircularProgressIndicator(color: Colors.white)
                    : Text(
                        'Submit for Verification',
                        style: TextStyle(fontSize: 16, color: Colors.white),
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildImageUploadCard({
    required String title,
    required File? image,
    required VoidCallback onTap,
  }) {
    return Card(
      elevation: 2,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: TextStyle(fontWeight: FontWeight.w600),
              ),
              SizedBox(height: 12),
              if (image == null)
                Container(
                  height: 150,
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.grey.shade300, width: 2),
                    borderRadius: BorderRadius.circular(8),
                    color: Colors.grey.shade50,
                  ),
                  child: Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.add_photo_alternate, size: 48, color: Colors.grey),
                        SizedBox(height: 8),
                        Text('Tap to upload', style: TextStyle(color: Colors.grey)),
                      ],
                    ),
                  ),
                )
              else
                Stack(
                  children: [
                    ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: Image.file(
                        image,
                        height: 150,
                        width: double.infinity,
                        fit: BoxFit.cover,
                      ),
                    ),
                    Positioned(
                      top: 8,
                      right: 8,
                      child: CircleAvatar(
                        backgroundColor: Colors.black54,
                        child: Icon(Icons.edit, color: Colors.white, size: 20),
                      ),
                    ),
                  ],
                ),
            ],
          ),
        ),
      ),
    );
  }

  Color _getStatusColor() {
    switch (_verificationStatus) {
      case 'Verified':
        return Colors.green;
      case 'Pending Review':
        return Colors.orange;
      case 'Rejected':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  IconData _getStatusIcon() {
    switch (_verificationStatus) {
      case 'Verified':
        return Icons.check_circle;
      case 'Pending Review':
        return Icons.pending;
      case 'Rejected':
        return Icons.cancel;
      default:
        return Icons.info;
    }
  }
}
```

---

## 🖥️ Part 2: Backend API (Spring Boot)

### 1. **Entity Classes**

```java
package com.verification.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "id_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IDVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @Column(nullable = false)
    private String idNumber;
    
    @Enumerated(EnumType.STRING)
    private IDType idType; // NATIONAL_ID, PASSPORT, DRIVERS_LICENSE
    
    // Document URLs
    private String frontIdImageUrl;
    private String backIdImageUrl;
    private String selfieImageUrl;
    
    // OCR Extracted Data
    @Column(columnDefinition = "TEXT")
    private String ocrExtractedData;
    
    private String extractedName;
    private String extractedIdNumber;
    private String extractedDateOfBirth;
    private String extractedAddress;
    
    // Verification Data
    @Enumerated(EnumType.STRING)
    private VerificationStatus status; // PENDING, APPROVED, REJECTED
    
    private Double confidenceScore; // OCR confidence (0-100)
    private Boolean nameMatch;
    private Boolean idNumberMatch;
    
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    
    @Column(columnDefinition = "TEXT")
    private String adminNotes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = VerificationStatus.PENDING;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

enum IDType {
    NATIONAL_ID,
    PASSPORT,
    DRIVERS_LICENSE,
    VOTER_ID
}

enum VerificationStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    REQUIRES_RESUBMISSION
}
```

### 2. **OCR Service (Using Google Cloud Vision API)**

```java
package com.verification.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

@Service
public class OCRService {
    
    // Extract text from ID using Google Vision API
    public Map<String, Object> extractIDInformation(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();
            
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);
            
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    throw new RuntimeException("Error: " + res.getError().getMessage());
                }
                
                String fullText = res.getFullTextAnnotation().getText();
                result.put("rawText", fullText);
                result.put("confidence", calculateConfidence(res));
                
                // Extract specific fields
                result.putAll(parseIDFields(fullText));
            }
        }
        
        return result;
    }
    
    // Parse ID-specific fields from extracted text
    private Map<String, String> parseIDFields(String text) {
        Map<String, String> fields = new HashMap<>();
        
        // Extract ID Number (various formats)
        Pattern idPattern = Pattern.compile("(?:ID|ID NO|NUMBER)\\s*:?\\s*([A-Z0-9]{5,20})", 
                                           Pattern.CASE_INSENSITIVE);
        Matcher idMatcher = idPattern.matcher(text);
        if (idMatcher.find()) {
            fields.put("idNumber", idMatcher.group(1));
        }
        
        // Extract Name
        Pattern namePattern = Pattern.compile("(?:NAME|FULL NAME)\\s*:?\\s*([A-Z\\s]{3,50})", 
                                            Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(text);
        if (nameMatcher.find()) {
            fields.put("name", nameMatcher.group(1).trim());
        }
        
        // Extract Date of Birth
        Pattern dobPattern = Pattern.compile("(?:DOB|DATE OF BIRTH|BORN)\\s*:?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
        Matcher dobMatcher = dobPattern.matcher(text);
        if (dobMatcher.find()) {
            fields.put("dateOfBirth", dobMatcher.group(1));
        }
        
        // Extract Address (simplified)
        Pattern addressPattern = Pattern.compile("(?:ADDRESS|ADDR)\\s*:?\\s*([\\w\\s,.-]{10,100})");
        Matcher addressMatcher = addressPattern.matcher(text);
        if (addressMatcher.find()) {
            fields.put("address", addressMatcher.group(1).trim());
        }
        
        return fields;
    }
    
    private double calculateConfidence(AnnotateImageResponse response) {
        if (response.getTextAnnotationsCount() > 0) {
            return response.getTextAnnotations(0).getConfidence() * 100;
        }
        return 0.0;
    }
    
    // Verify if selfie matches ID photo (using Face Detection)
    public boolean verifySelfieMatch(MultipartFile selfie, MultipartFile idPhoto) throws IOException {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            // Detect faces in both images
            List<FaceAnnotation> selfieFaces = detectFaces(vision, selfie);
            List<FaceAnnotation> idFaces = detectFaces(vision, idPhoto);
            
            if (selfieFaces.isEmpty() || idFaces.isEmpty()) {
                return false;
            }
            
            // Compare face landmarks (simplified comparison)
            // In production, use proper face recognition API
            return compareFaces(selfieFaces.get(0), idFaces.get(0));
        }
    }
    
    private List<FaceAnnotation> detectFaces(ImageAnnotatorClient vision, MultipartFile file) throws IOException {
        ByteString imgBytes = ByteString.copyFrom(file.getBytes());
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        
        BatchAnnotateImagesResponse response = vision.batchAnnotateImages(Collections.singletonList(request));
        return response.getResponses(0).getFaceAnnotationsList();
    }
    
    private boolean compareFaces(FaceAnnotation face1, FaceAnnotation face2) {
        // Simplified comparison - in production use proper face recognition
        // Compare joy, sorrow, anger likelihoods as basic matching
        return Math.abs(face1.getJoyLikelihood().getNumber() - face2.getJoyLikelihood().getNumber()) < 2;
    }
}
```

### 3. **Verification Controller**

```java
package com.verification.controller;

import com.verification.dto.*;
import com.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationController {
    
    private final VerificationService verificationService;
    
    // User submits verification
    @PostMapping("/submit")
    public ResponseEntity<VerificationResponse> submitVerification(
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("idNumber") String idNumber,
            @RequestParam("frontId") MultipartFile frontId,
            @RequestParam(value = "backId", required = false) MultipartFile backId,
            @RequestParam("selfie") MultipartFile selfie,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        VerificationRequest request = new VerificationRequest();
        request.setFullName(name);
        request.setPhoneNumber(phone);
        request.setIdNumber(idNumber);
        request.setFrontIdImage(frontId);
        request.setBackIdImage(backId);
        request.setSelfieImage(selfie);
        
        VerificationResponse response = verificationService.submitVerification(
                request, userDetails.getUsername()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Get user's verification status
    @GetMapping("/status")
    public ResponseEntity<VerificationResponse> getVerificationStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        VerificationResponse response = verificationService.getUserVerificationStatus(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(response);
    }
    
    // Admin: Get all pending verifications
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VerificationResponse>> getPendingVerifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<VerificationResponse> verifications = verificationService.getPendingVerifications(page, size);
        return ResponseEntity.ok(verifications);
    }
    
    // Admin: Get verification details
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VerificationDetailResponse> getVerificationDetails(@PathVariable Long id) {
        VerificationDetailResponse response = verificationService.getVerificationDetails(id);
        return ResponseEntity.ok(response);
    }
    
    // Admin: Approve verification
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveVerification(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReviewRequest reviewRequest,
            @AuthenticationPrincipal UserDetails adminDetails
    ) {
        verificationService.approveVerification(id, adminDetails.getUsername(), reviewRequest);
        return ResponseEntity.ok("Verification approved");
    }
    
    // Admin: Reject verification
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rejectVerification(
            @PathVariable Long id,
            @RequestBody AdminReviewRequest reviewRequest,
            @AuthenticationPrincipal UserDetails adminDetails
    ) {
        verificationService.rejectVerification(id, adminDetails.getUsername(), reviewRequest);
        return ResponseEntity.ok("Verification rejected");
    }
}
```

### 4. **Verification Service**

```java
package com.verification.service;

import com.verification.entity.*;
import com.verification.repository.*;
import com.verification.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerificationService {
    
    private final IDVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final OCRService ocrService;
    private final NotificationService notificationService;
    
    @Transactional
    public VerificationResponse submitVerification(VerificationRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already has pending verification
        Optional<IDVerification> existing = verificationRepository
                .findByUserAndStatus(user, VerificationStatus.PENDING);
        if (existing.isPresent()) {
            throw new RuntimeException("You already have a pending verification");
        }
        
        IDVerification verification = new IDVerification();
        verification.setUser(user);
        verification.setFullName(request.getFullName());
        verification.setPhoneNumber(request.getPhoneNumber());
        verification.setIdNumber(request.getIdNumber());
        
        try {
            // Upload images to Cloudinary
            String frontIdUrl = cloudinaryService.uploadImage(
                    request.getFrontIdImage(), "verifications/front"
            );
            verification.setFrontIdImageUrl(frontIdUrl);
            
            if (request.getBackIdImage() != null) {
                String backIdUrl = cloudinaryService.uploadImage(
                        request.getBackIdImage(), "verifications/back"
                );
                verification.setBackIdImageUrl(backIdUrl);
            }
            
            String selfieUrl = cloudinaryService.uploadImage(
                    request.getSelfieImage(), "verifications/selfie"
            );
            verification.setSelfieImageUrl(selfieUrl);
            
            // Perform OCR on front ID
            Map<String, Object> ocrResult = ocrService.extractIDInformation(
                    request.getFrontIdImage()
            );
            
            verification.setOcrExtractedData(ocrResult.toString());
            verification.setExtractedName((String) ocrResult.get("name"));
            verification.setExtractedIdNumber((String) ocrResult.get("idNumber"));
            verification.setExtractedDateOfBirth((String) ocrResult.get("dateOfBirth"));
            verification.setExtractedAddress((String) ocrResult.get("address"));
            verification.setConfidenceScore((Double) ocrResult.get("confidence"));
            
            // Auto-verify matches
            verification.setNameMatch(checkNameMatch(
                    request.getFullName(), 
                    verification.getExtractedName()
            ));
            verification.setIdNumberMatch(
                    request.getIdNumber().equalsIgnoreCase(verification.getExtractedIdNumber())
            );
            
            // If confidence is high and data matches, auto-approve
            if (verification.getConfidenceScore() > 95.0 && 
                verification.getNameMatch() && 
                verification.getIdNumberMatch()) {
                verification.setStatus(VerificationStatus.APPROVED);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process verification: " + e.getMessage());
        }
        
        IDVerification saved = verificationRepository.save(verification);
        
        // Notify admins
        notificationService.notifyAdminsNewVerification(saved);
        
        return toResponse(saved);
    }
    
    private boolean checkNameMatch(String providedName, String extractedName) {
        if (extractedName == null) return false;
        
        String provided = providedName.toLowerCase().replaceAll("[^a-z\\s]", "");
        String extracted = extractedName.toLowerCase().replaceAll("[^a-z\\s]", "");
        
        // Simple similarity check
        return extracted.contains(provided) || provided.contains(extracted);
    }
    
    public List<VerificationResponse> getPendingVerifications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IDVerification> verifications = verificationRepository.findByStatus(
                VerificationStatus.PENDING, pageable
        );
        
        return verifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public VerificationDetailResponse getVerificationDetails(Long id) {
        IDVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        return toDetailResponse(verification);
    }
    
    @Transactional
    public void approveVerification(Long id, String adminUsername, AdminReviewRequest request) {
        IDVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        verification.setStatus(VerificationStatus.APPROVED);
        verification.setReviewedBy(admin);
        verification.setReviewedAt(java.time.LocalDateTime.now());
        if (request != null && request.getNotes() != null) {
            verification.setAdminNotes(request.getNotes());
        }
        
        verificationRepository.save(verification);
        
        // Update user's verified status
        verification.getUser().setIdVerified(true);
        userRepository.save(verification.getUser());
        
        // Notify user
        notificationService.notifyVerificationApproved(verification);
    }
    
    @Transactional
    public void rejectVerification(Long id, String adminUsername, AdminReviewRequest request) {
        IDVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        verification.setStatus(VerificationStatus.REJECTED);
        verification.setReviewedBy(admin);
        verification.setReviewedAt(java.time.LocalDateTime.now());
        verification.setRejectionReason(request.getReason());
        if (request.getNotes() != null) {
            verification.setAdminNotes(request.getNotes());
        }
        
        verificationRepository.save(verification);
        
        // Notify user
        notificationService.notifyVerificationRejected(verification);
    }
    
    public VerificationResponse getUserVerificationStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<IDVerification> verification = verificationRepository
                .findTopByUserOrderByCreatedAtDesc(user);
        
        return verification.map(this::toResponse).orElse(null);
    }
    
    private VerificationResponse toResponse(IDVerification verification) {
        VerificationResponse response = new VerificationResponse();
        response.setId(verification.getId());
        response.setStatus(verification.getStatus().name());
        response.setCreatedAt(verification.getCreatedAt());
        response.setConfidenceScore(verification.getConfidenceScore());
        response.setNameMatch(verification.getNameMatch());
        response.setIdNumberMatch(verification.getIdNumberMatch());
        return response;
    }
    
    private VerificationDetailResponse toDetailResponse(IDVerification verification) {
        VerificationDetailResponse response = new VerificationDetailResponse();
        response.setId(verification.getId());
        response.setFullName(verification.getFullName());
        response.setPhoneNumber(verification.getPhoneNumber());
        response.setIdNumber(verification.getIdNumber());
        response.setFrontIdImageUrl(verification.getFrontIdImageUrl());
        response.setBackIdImageUrl(verification.getBackIdImageUrl());
        response.setSelfieImageUrl(verification.getSelfieImageUrl());
        response.setExtractedName(verification.getExtractedName());
        response.setExtractedIdNumber(verification.getExtractedIdNumber());
        response.setExtractedDateOfBirth(verification.getExtractedDateOfBirth());
        response.setExtractedAddress(verification.getExtractedAddress());
        response.setConfidenceScore(verification.getConfidenceScore());
        response.setNameMatch(verification.getNameMatch());
        response.setIdNumberMatch(verification.getIdNumberMatch());
        response.setStatus(verification.getStatus().name());
        response.setCreatedAt(verification.getCreatedAt());
        response.setReviewedAt(verification.getReviewedAt());
        response.setAdminNotes(verification.getAdminNotes());
        response.setRejectionReason(verification.getRejectionReason());
        return response;
    }
}
```

---

## 🎛️ Part 3: Admin Dashboard (React)

```jsx
import React, { useState, useEffect } from 'react';
import { Card, Button, Badge, Modal, Image, Spin, message } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined, EyeOutlined } from '@ant-design/icons';
import axios from 'axios';

const AdminVerificationDashboard = () => {
  const [verifications, setVerifications] = useState([]);
  const [selectedVerification, setSelectedVerification] = useState(null);
  const [loading, setLoading] = useState(false);
  const [detailsVisible, setDetailsVisible] = useState(false);

  useEffect(() => {
    loadPendingVerifications();
  }, []);

  const loadPendingVerifications = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/api/verification/admin/pending', {
        headers: { Authorization: `Bearer ${getToken()}` }
      });
      setVerifications(response.data);
    } catch (error) {
      message.error('Failed to load verifications');
    } finally {
      setLoading(false);
    }
  };

  const viewDetails = async (id) => {
    try {
      const response = await axios.get(`/api/verification/admin/${id}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      });
      setSelectedVerification(response.data);
      setDetailsVisible(true);
    } catch (error) {
      message.error('Failed to load details');
    }
  };

  const approveVerification = async (id) => {
    try {
      await axios.post(`/api/verification/admin/${id}/approve`, null, {
        headers: { Authorization: `Bearer ${getToken()}` }
      });
      message.success('Verification approved');
      loadPendingVerifications();
      setDetailsVisible(false);
    } catch (error) {
      message.error('Failed to approve');
    }
  };

  const rejectVerification = async (id, reason) => {
    try {
      await axios.post(`/api/verification/admin/${id}/reject`, 
        { reason },
        { headers: { Authorization: `Bearer ${getToken()}` } }
      );
      message.success('Verification rejected');
      loadPendingVerifications();
      setDetailsVisible(false);
    } catch (error) {
      message.error('Failed to reject');
    }
  };

  const getToken = () => localStorage.getItem('token');

  return (
    <div style={{ padding: '24px' }}>
      <h1>ID Verification Dashboard</h1>
      
      <Spin spinning={loading}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' }}>
          {verifications.map(v => (
            <Card
              key={v.id}
              title={`Verification #${v.id}`}
              extra={
                <Badge 
                  status={v.nameMatch && v.idNumberMatch ? 'success' : 'warning'}
                  text={v.nameMatch && v.idNumberMatch ? 'Match' : 'Review'}
                />
              }
              actions={[
                <Button 
                  icon={<EyeOutlined />} 
                  onClick={() => viewDetails(v.id)}
                >
                  View
                </Button>
              ]}
            >
              <p><strong>Confidence:</strong> {v.confidenceScore?.toFixed(1)}%</p>
              <p><strong>Submitted:</strong> {new Date(v.createdAt).toLocaleString()}</p>
              <p>
                <strong>Name Match:</strong> 
                {v.nameMatch ? ' ✓' : ' ✗'}
              </p>
              <p>
                <strong>ID Match:</strong> 
                {v.idNumberMatch ? ' ✓' : ' ✗'}
              </p>
            </Card>
          ))}
        </div>
      </Spin>

      <Modal
        title="Verification Details"
        visible={detailsVisible}
        onCancel={() => setDetailsVisible(false)}
        width={900}
        footer={[
          <Button 
            key="reject" 
            danger
            icon={<CloseCircleOutlined />}
            onClick={() => {
              const reason = prompt('Rejection reason:');
              if (reason) rejectVerification(selectedVerification.id, reason);
            }}
          >
            Reject
          </Button>,
          <Button 
            key="approve" 
            type="primary"
            icon={<CheckCircleOutlined />}
            onClick={() => approveVerification(selectedVerification.id)}
          >
            Approve
          </Button>
        ]}
      >
        {selectedVerification && (
          <div>
            <h3>Submitted Information</h3>
            <p><strong>Full Name:</strong> {selectedVerification.fullName}</p>
            <p><strong>Phone:</strong> {selectedVerification.phoneNumber}</p>
            <p><strong>ID Number:</strong> {selectedVerification.idNumber}</p>

            <h3>Extracted Information (OCR)</h3>
            <p><strong>Name:</strong> {selectedVerification.extractedName}</p>
            <p><strong>ID Number:</strong> {selectedVerification.extractedIdNumber}</p>
            <p><strong>DOB:</strong> {selectedVerification.extractedDateOfBirth}</p>
            <p><strong>Address:</strong> {selectedVerification.extractedAddress}</p>
            <p><strong>Confidence:</strong> {selectedVerification.confidenceScore?.toFixed(1)}%</p>

            <h3>Uploaded Documents</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
              <div>
                <p><strong>Front ID</strong></p>
                <Image src={selectedVerification.frontIdImageUrl} />
              </div>
              {selectedVerification.backIdImageUrl && (
                <div>
                  <p><strong>Back ID</strong></p>
                  <Image src={selectedVerification.backIdImageUrl} />
                </div>
              )}
              <div>
                <p><strong>Selfie</strong></p>
                <Image src={selectedVerification.selfieImageUrl} />
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AdminVerificationDashboard;
```

---

## 📊 Part 4: Database Schema

```sql
CREATE TABLE id_verifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    id_number VARCHAR(50) NOT NULL,
    id_type VARCHAR(50),
    
    -- Document URLs
    front_id_image_url TEXT,
    back_id_image_url TEXT,
    selfie_image_url TEXT,
    
    -- OCR Data
    ocr_extracted_data TEXT,
    extracted_name VARCHAR(255),
    extracted_id_number VARCHAR(50),
    extracted_date_of_birth VARCHAR(50),
    extracted_address TEXT,
    
    -- Verification
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    confidence_score DECIMAL(5,2),
    name_match BOOLEAN,
    id_number_match BOOLEAN,
    
    -- Admin Review
    reviewed_by BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,
    rejection_reason TEXT,
    admin_notes TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_verifications_user ON id_verifications(user_id);
CREATE INDEX idx_verifications_status ON id_verifications(status);
CREATE INDEX idx_verifications_created ON id_verifications(created_at DESC);
```

---

## 🔒 Security Best Practices

1. **Encrypt stored IDs at rest**
2. **Use HTTPS for all transfers**
3. **Implement rate limiting**
4. **Log all admin actions**
5. **Auto-delete rejected documents after 30 days**
6. **Require 2FA for admin access**

This complete system provides OCR scanning, automatic matching, and admin verification workflow! 🎯