package de.rieckpil.courses.book.review;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static de.rieckpil.courses.book.review.RandomReviewParameterResolverExtension.RandomReview;

@ExtendWith(RandomReviewParameterResolverExtension.class)
class ReviewVerifierTest {

  private ReviewVerifier reviewVerifier;

  @BeforeEach
  public void setUp() {
    reviewVerifier = new ReviewVerifier();
  }

  @Test
  void shouldFailWhenReviewContainsSwearWord() {

    var review = "This book is shit";

    var result = reviewVerifier.doesMeetQualityStandards(review);
    Assertions.assertFalse(result, "ReviewVerifier did not detect sear word");
  }

  @Test
  @DisplayName("Should fail when review contains 'lorem ipsum'")
  void testLoremIpsum() {
    String review = "Lorem Ipsum generated text...";

    var result = reviewVerifier.doesMeetQualityStandards(review);
    Assertions.assertFalse(result, "ReviewVerifier did not detect Lorem Ipsum");
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/badReview.csv")
  void shouldFailWhenReviewIsOfBadQuality(String review) {

    var result = reviewVerifier.doesMeetQualityStandards(review);
    Assertions.assertFalse(result, "ReviewVerifier did not detect bad review");

  }

  @RepeatedTest(5)
  void shouldFailWhenRandomReviewQualityIsBad(@RandomReview String review) throws InterruptedException {
    Thread.sleep(1000);
    var result = reviewVerifier.doesMeetQualityStandards(review);
    Assertions.assertFalse(result, "ReviewVerifier did not detect random review");
  }

  @Test
  void shouldPassWhenReviewIsGood() throws InterruptedException {
    Thread.sleep(1000);
    var review = "I can totally recommend this book " +
      "who is interested in learning hwo to write Java code!";

    var result = reviewVerifier.doesMeetQualityStandards(review);
    Assertions.assertTrue(result, "ReviewVerifier did not pass a good review");
  }

  @Test
  void shouldPassWhenReviewIsGoodHamcrest() throws InterruptedException {
    Thread.sleep(1000);
    var review = "I can totally recommend this book " +
      "who is interested in learning hwo to write Java code!";

    var result = reviewVerifier.doesMeetQualityStandards(review);

    MatcherAssert.assertThat("ReviewVerifier did not pass a good review", result, Matchers.is(true));
    MatcherAssert.assertThat("Lorem ipsum", Matchers.endsWith("ipsum"));
    MatcherAssert.assertThat(List.of(1, 2, 3, 4, 5), Matchers.hasSize(5));
  }

}
