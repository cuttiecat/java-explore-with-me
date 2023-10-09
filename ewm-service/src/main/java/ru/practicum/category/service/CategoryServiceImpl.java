package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.CategoryIsNotEmptyException;
import ru.practicum.exception.NotFoundException;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new AlreadyExistsException("Категория с таким именем " + newCategoryDto.getName() + " уже существует.");
        }
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        if (categoryDto.getName() == null || categoryDto.getName().isEmpty()) {
            throw new RuntimeException("Название категории не может быть пустым");
        }
        Category savedCategory = checkCategoryExistAndGet(catId);
        if (!savedCategory.getName().equals(categoryDto.getName()) && categoryRepository.existsByName(categoryDto.getName())) {
            throw new AlreadyExistsException("Категория с таким именем уже существует: " + categoryDto.getName());
        } else {
            savedCategory.setName(categoryDto.getName());
            return categoryMapper.toCategoryDto(categoryRepository.save(savedCategory));
        }
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
        if (eventRepository.existsByCategoryId(catId)) {
            throw new CategoryIsNotEmptyException("Эта категория не пуста.");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional
    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
        return categoryMapper.toCategoryDtoList(categoryRepository.findAll(pageable).toList());
    }

    @Transactional
    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = checkCategoryExistAndGet(catId);
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public Category checkCategoryExistAndGet(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория с id " + catId + " не найдена"));
    }

}
