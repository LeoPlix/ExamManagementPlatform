package pt.ulisboa.tecnico.rnl.dei.ems.statistics;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.ems.statistics.dto.StatisticsDto;
import pt.ulisboa.tecnico.rnl.dei.ems.statistics.service.StatisticsService;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

	private final StatisticsService statisticsService;

	public StatisticsController(StatisticsService statisticsService) {
		this.statisticsService = statisticsService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('STATISTICS_READ')")
	public StatisticsDto getStatistics() {
		return statisticsService.getStatistics();
	}
}
