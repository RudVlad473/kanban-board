package com.vrudenko.kanban_board.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;
import com.vrudenko.kanban_board.dto.column_dto.ColumnResponseDTO;
import com.vrudenko.kanban_board.dto.column_dto.SaveColumnRequestDTO;
import com.vrudenko.kanban_board.dto.task_dto.SaveTaskRequestDTO;
import com.vrudenko.kanban_board.dto.task_dto.TaskResponseDTO;
import com.vrudenko.kanban_board.entity.BoardEntity;
import com.vrudenko.kanban_board.entity.ColumnEntity;
import com.vrudenko.kanban_board.exception.AppEntityNotFoundException;
import com.vrudenko.kanban_board.mapper.ColumnMapper;
import com.vrudenko.kanban_board.repository.ColumnRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ColumnService {
  @Autowired private ColumnRepository columnRepository;
  @Autowired private ColumnMapper columnMapper;
  @Autowired private TaskService taskService;
  @Autowired private OwnershipVerifierService ownershipVerifierService;

  @Transactional
  public void deleteAllByBoardId(String userId, String boardId) {
    // TODO: figure out how to optimize ownership verification calls
    var pair = ownershipVerifierService.verifyOwnershipOfBoard(userId, boardId);

    for (var column : findAllByBoardId(userId, boardId)) {
      taskService.deleteAllByColumnId(userId, column.getId());
    }

    columnRepository.deleteAllByBoardId(pair.getSecond().getId());
  }

  public ColumnResponseDTO save(SaveColumnRequestDTO columnDTO, BoardEntity board) {
    var column = columnMapper.fromSaveColumnRequestDTO(columnDTO);
    column.setBoard(board);

    columnRepository.save(column);

    return columnMapper.toColumnResponseDTO(column);
  }

  public TaskResponseDTO addTaskByColumnId(
      String userId, String columnId, SaveTaskRequestDTO taskDTO) {
    var column = findById(userId, columnId);

    return taskService.save(taskDTO, column);
  }

  public ColumnEntity findById(String userId, String columnId) {
    var pair = ownershipVerifierService.verifyOwnershipOfColumn(userId, columnId);

    return pair.getSecond();
  }

  @VisibleForTesting
  public int getColumnCountByBoardId(String boardId) {
    return Ints.checkedCast(columnRepository.countByBoardId(boardId));
  }

  public List<ColumnResponseDTO> findAllByBoardId(String userId, String boardId) {
    var pair = ownershipVerifierService.verifyOwnershipOfBoard(userId, boardId);

    return columnMapper.toColumnResponseDTOList(
        columnRepository.findAllByBoardId(pair.getSecond().getId()));
  }

  // TODO: implement delete logic
}
