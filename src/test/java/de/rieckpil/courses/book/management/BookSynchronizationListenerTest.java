package de.rieckpil.courses.book.management;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSynchronizationListenerTest {

  private final static String VALID_ISBN = "1234567891234";

  @Mock
  private BookRepository bookRepository;

  @Mock
  private OpenLibraryApiClient openLibraryApiClient;

  @InjectMocks
  private BookSynchronizationListener cut;

  @Captor
  private ArgumentCaptor<Book> bookArgumentCaptor;

  @Test
  void shouldRejectBookWhenIsbnIsMalformed() {

    var bookSynchronization = new BookSynchronization("42");

    cut.consumeBookUpdates(bookSynchronization);
    Mockito.verifyNoInteractions(openLibraryApiClient, bookRepository);
  }

  @Test
  void shouldNotOverrideWhenBookAlreadyExists() {
    var bookSynchronization = new BookSynchronization(VALID_ISBN);
    Mockito.when(bookRepository.findByIsbn(VALID_ISBN)).thenReturn(new Book());

    cut.consumeBookUpdates(bookSynchronization);

    Assertions.assertAll(() -> {
      Mockito.verifyNoInteractions(openLibraryApiClient);
      Mockito.verify(bookRepository, Mockito.times(1)).findByIsbn(VALID_ISBN);
      Mockito.verify(bookRepository, times(0)).save(ArgumentMatchers.any(Book.class));
    });

  }

  @Test
  void shouldThrowExceptionWhenProcessingFails() {
    var bookSynchronization = new BookSynchronization(VALID_ISBN);
    Mockito.when(bookRepository.findByIsbn(VALID_ISBN)).thenReturn(null);

    when(openLibraryApiClient.fetchMetadataForBook(VALID_ISBN)).thenThrow(new RuntimeException("Network timeout"));
    Assertions.assertThrows(RuntimeException.class, () -> cut.consumeBookUpdates(bookSynchronization));
  }

  @Test
  void shouldStoreBookWhenNewAndCorrectIsbn() {
    var bookSynchronization = new BookSynchronization(VALID_ISBN);
    Mockito.when(bookRepository.findByIsbn(VALID_ISBN)).thenReturn(null);

    var requestedBook = new Book();
    requestedBook.setTitle("Java Book");
    requestedBook.setIsbn(VALID_ISBN);

    Mockito.when(openLibraryApiClient.fetchMetadataForBook(VALID_ISBN)).thenReturn(requestedBook);
    Mockito.when(bookRepository.save(ArgumentMatchers.any())).then(invocation -> {
      Book methodArgument = invocation.getArgument(0);
      methodArgument.setId(1L);

      return methodArgument;
    });

    cut.consumeBookUpdates(bookSynchronization);

    Mockito.verify(bookRepository).save(bookArgumentCaptor.capture());

    Assertions.assertEquals("Java Book", bookArgumentCaptor.getValue().getTitle());
    Assertions.assertEquals(VALID_ISBN, bookArgumentCaptor.getValue().getIsbn());
  }

}
