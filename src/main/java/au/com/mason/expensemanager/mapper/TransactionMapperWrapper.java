package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Transaction;
import au.com.mason.expensemanager.dto.TransactionDto;

public interface TransactionMapperWrapper<T extends Transaction, V extends TransactionDto> {
	
	public T transactionDtoToTransaction(V transactionDto) throws Exception;
    public T transactionDtoToTransaction(V transactionDto, T transaction) throws Exception;
    public V transactionToTransactionDto(T transaction) throws Exception;

}
