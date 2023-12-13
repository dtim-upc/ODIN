package edu.upc.essi.dtim.odin.datasets;

public class SourceServiceTest {
//
//    private SourceService sourceService;
//    private AppConfig appConfig;
//    private ProjectService projectService;
//    private ORMStoreInterface ormDataResource;
//
//    @BeforeEach
//    public void setUp() {
//        appConfig = mock(AppConfig.class);
//        projectService = mock(ProjectService.class);
//        ormDataResource = mock(ORMStoreInterface.class);
//        sourceService = new SourceService(appConfig, projectService);
//    }
//
//    @Test
//    public void testReconstructFile() {
//        try {
//            // Arrange
//            // Mock the appConfig.getDiskPath() method to return a temporary directory path
//            when(appConfig.getDataLayerPath()).thenReturn(System.getProperty("java.io.tmpdir"));
//
//            // Create a test MultipartFile
//            String originalFilename = "test.csv";
//            MultipartFile multipartFile = new MockMultipartFile(
//                    "file",
//                    originalFilename,
//                    "text/csv",
//                    "file content".getBytes()
//            );
//
//            // Act
//            String filePath = sourceService.reconstructFile(multipartFile);
//
//            // Assert
//            assertNotNull(filePath);
//            assertTrue(Files.exists(Path.of(filePath)));
//
//            // Clean up: Delete the created file
//            Files.delete(Path.of(filePath));
//        } catch (IOException e) {
//            fail("IOException occurred: " + e.getMessage());
//        }
//    }
//
//    @Test
//    public void testReconstructFileEmptyFile() {
//        // Arrange
//        MultipartFile multipartFile = new MockMultipartFile(
//                "file",
//                "empty.csv",
//                "text/csv",
//                new byte[0]
//        );
//
//        // Act and Assert
//        assertThrows(RuntimeException.class, () -> sourceService.reconstructFile(multipartFile));
//    }
//
//    @Test
//    public void testReconstructFileOutsideCurrentDirectory() {
//        // Arrange
//        // Mock the appConfig.getDiskPath() method to return the root directory (which is not safe)
//        when(appConfig.getDataLayerPath()).thenReturn("/");
//
//        // Create a test MultipartFile
//        String originalFilename = "test.csv";
//        MultipartFile multipartFile = new MockMultipartFile(
//                "file",
//                originalFilename,
//                "text/csv",
//                "file content".getBytes()
//        );
//
//        // Act and Assert
//        assertThrows(RuntimeException.class, () -> sourceService.reconstructFile(multipartFile));
//    }
}
