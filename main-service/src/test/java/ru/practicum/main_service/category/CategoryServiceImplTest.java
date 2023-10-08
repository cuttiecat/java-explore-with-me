package ru.practicum.main_service.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.category.dto.CategoryDto;
import ru.practicum.main_service.category.dto.NewCategoryDto;
import ru.practicum.main_service.category.mapper.CategoryMapper;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.category.repository.CategoryRepository;
import ru.practicum.main_service.category.service.CategoryServiceImpl;
import ru.practicum.main_service.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Captor
    private ArgumentCaptor<Category> categoryArgumentCaptor;

    private final NewCategoryDto newCategoryDto = NewCategoryDto.builder()
            .name("test category 1")
            .build();
    private final Category category1 = Category.builder()
            .id(1L)
            .name(newCategoryDto.getName())
            .build();
    private final Category category2 = Category.builder()
            .id(2L)
            .name("test category 2")
            .build();
    private CategoryDto categoryDto1;
    private CategoryDto categoryDto2;
    private final Integer from = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_FROM);
    private final Integer size = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);

    @BeforeEach
    public void beforeEach() {
        categoryDto2 = CategoryDto.builder()
                .id(2L)
                .name("test category 2")
                .build();
        categoryDto1 = CategoryDto.builder()
                .id(1L)
                .name("test category 1")
                .build();
    }


    @Nested
    class Create {
        @Test
        public void shouldCreate() {
            when(categoryMapper.newCategoryDtoToCategory(any())).thenReturn(category1);
            when(categoryMapper.toCategoryDto(any())).thenReturn(categoryDto1);
            when(categoryRepository.save(any())).thenReturn(category1);


            CategoryDto savedCategoryDto = categoryService.create(newCategoryDto);

            checkResult(category1, savedCategoryDto);

            verify(categoryMapper, times(1)).newCategoryDtoToCategory(any());
            verify(categoryMapper, times(1)).toCategoryDto(any());
            verify(categoryRepository, times(1)).save(categoryArgumentCaptor.capture());

            Category savedCategory = categoryArgumentCaptor.getValue();

            assertEquals(newCategoryDto.getName(), savedCategory.getName());
        }
    }

    @Nested
    class GetAll {
        @Test
        public void shouldGetAll() {
            when(categoryMapper.toCategoryDto(category1)).thenReturn(categoryDto1);
            when(categoryMapper.toCategoryDto(category2)).thenReturn(categoryDto2);
            when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category1, category2)));

            List<CategoryDto> categoriesDtoFromService = categoryService.getAll(pageable);

            assertEquals(2, categoriesDtoFromService.size());
            checkResult(category1, categoriesDtoFromService.get(0));
            checkResult(category2, categoriesDtoFromService.get(1));

            verify(categoryMapper, times(2)).toCategoryDto(any());
            verify(categoryRepository, times(1)).findAll(pageable);
        }

        @Test
        public void shouldGetOne() {
            when(categoryMapper.toCategoryDto(category2)).thenReturn(categoryDto2);
            when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category2)));

            List<CategoryDto> categoriesDtoFromService = categoryService.getAll(pageable);

            assertEquals(1, categoriesDtoFromService.size());
            checkResult(category2, categoriesDtoFromService.get(0));

            verify(categoryMapper, times(1)).toCategoryDto(any());
            verify(categoryRepository, times(1)).findAll(pageable);
        }

        @Test
        public void shouldGetEmpty() {
            when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

            List<CategoryDto> categoriesDtoFromService = categoryService.getAll(pageable);

            assertTrue(categoriesDtoFromService.isEmpty());

            verify(categoryRepository, times(1)).findAll(pageable);
        }
    }

    @Nested
    class GetById {
        @Test
        public void shouldGet() {
            when(categoryMapper.toCategoryDto(any())).thenReturn(categoryDto1);
            when(categoryRepository.findById(any())).thenReturn(Optional.of(category1));

            CategoryDto categoryDtoFromService = categoryService.getById(category1.getId());

            checkResult(category1, categoryDtoFromService);

            verify(categoryMapper, times(1)).toCategoryDto(any());
            verify(categoryRepository, times(1)).findById(any());
        }

        @Test
        public void shouldThrowExceptionIfIdNotFound() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> categoryService.getById(99L));
            assertEquals("Категории с таким id не существует.", exception.getMessage());

            verify(categoryRepository, times(1)).findById(any());
        }
    }

    @Nested
    class Patch {
        @Test
        public void shouldPatch() {
            when(categoryMapper.toCategoryDto(any())).thenReturn(categoryDto2);
            when(categoryMapper.categoryDtoToCategory(any())).thenReturn(category1);
            when(categoryRepository.findById(category1.getId())).thenReturn(Optional.of(category1));
            when(categoryRepository.save(any()))
                    .thenReturn(Category.builder()
                            .id(category1.getId())
                            .name(categoryDto2.getName())
                            .build());

            CategoryDto categoryDtoFromService = categoryService.patch(category1.getId(), categoryDto2);

            assertEquals(category1.getId(), categoryDtoFromService.getId());
            assertEquals(categoryDto2.getName(), categoryDtoFromService.getName());

            verify(categoryMapper, times(1)).toCategoryDto(any());
            verify(categoryMapper, times(1)).categoryDtoToCategory(any());
            verify(categoryRepository, times(1)).findById(any());
            verify(categoryRepository, times(1)).save(categoryArgumentCaptor.capture());

            Category savedCategory = categoryArgumentCaptor.getValue();

            assertEquals(category1.getId(), savedCategory.getId());
            assertEquals(categoryDto1.getName(), savedCategory.getName());
        }

        @Test
        public void shouldThrowExceptionIfIdNotFound() {
            when(categoryRepository.findById(category1.getId())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> categoryService.patch(category1.getId(), categoryDto2));
            assertEquals("Категории с таким id не существует.", exception.getMessage());

            verify(categoryRepository, times(1)).findById(any());
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteById {
        @Test
        public void shouldDelete() {
            when(categoryRepository.existsById(category1.getId())).thenReturn(true);

            categoryService.deleteById(category1.getId());

            verify(categoryRepository, times(1)).deleteById(any());
        }

        @Test
        public void shouldThrowExceptionIfIdNotFound() {
            when(categoryRepository.existsById(99L)).thenReturn(false);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> categoryService.deleteById(99L));
            assertEquals("Категории с id:'99' не существует.", exception.getMessage());

            verify(categoryRepository, never()).deleteById(any());
        }
    }

    @Nested
    class GetCategoryById {
        @Test
        public void shouldGet() {
            when(categoryRepository.findById(category2.getId())).thenReturn(Optional.of(category2));

            Category categoryFromService = categoryService.getCategoryById(category2.getId());

            assertEquals(category2, categoryFromService);

            verify(categoryRepository, times(1)).findById(any());
        }

        @Test
        public void shouldThrowExceptionIfIdNotFound() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> categoryService.getCategoryById(99L));
            assertEquals("Категории с таким id не существует.", exception.getMessage());

            verify(categoryRepository, times(1)).findById(any());
        }
    }

    private void checkResult(Category category, CategoryDto categoryDto) {
        assertEquals(category.getId(), categoryDto.getId());
        assertEquals(category.getName(), categoryDto.getName());
    }
}
