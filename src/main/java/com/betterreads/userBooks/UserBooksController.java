package com.betterreads.userBooks;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import com.betterreads.books.Book;
import com.betterreads.books.BookRepository;
import com.betterreads.user.BooksByUser;
import com.betterreads.user.BooksByUserRepository;

@Controller
public class UserBooksController {

	@Autowired
	private UserBooksRepository userBookRepository;

	@Autowired
	BooksByUserRepository booksByUserRepository;

	@Autowired
	BookRepository bookRepository;

	@PostMapping("/addUserBook")
	public ModelAndView addBookForUser(@RequestBody MultiValueMap<String, String> formData,
			@AuthenticationPrincipal OAuth2User principal) {

		if (principal == null || principal.getAttribute("login") == null) {
			return null;
		}

		String bookId = formData.getFirst("bookId");
		String userId = principal.getAttribute("login");
		Optional<Book> optionalBook = bookRepository.findById(bookId);
		if (!optionalBook.isPresent()) {
			return new ModelAndView("redirect:/");
		}

		Book book = optionalBook.get();

//		System.out.println(formData);

		UserBooks userBooks = new UserBooks();
		UserBooksPrimaryKey key = new UserBooksPrimaryKey();

		key.setUserId(userId);
		key.setBookId(bookId);

		userBooks.setKey(key);

		int rating = Integer.parseInt(formData.getFirst("rating"));
		userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startedDate")));
		userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
		userBooks.setRating(rating);
		userBooks.setReadingStatus(formData.getFirst("readingStatus"));

		userBookRepository.save(userBooks);

		BooksByUser booksByUser = new BooksByUser();
		booksByUser.setId(userId);
		booksByUser.setBookId(bookId);
		booksByUser.setBookName(book.getName());
		booksByUser.setCoverIds(book.getCoverIds());
		booksByUser.setAuthorNames(book.getAuthorNames());
		booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
		booksByUser.setRating(rating);
		booksByUserRepository.save(booksByUser);

		return new ModelAndView("redirect:/books/" + bookId);

	}
}
