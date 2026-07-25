package com.bitsystem.bitapp.dto;

import com.bitsystem.bitapp.domain.Mentor;
import java.util.Arrays;
import java.util.List;

public record MentorDto(
    Long id,
    String nome,
    String papel,
    List<String> areasAtendidas,
    String linkSala
) {
    public static MentorDto from(Mentor m) {
        List<String> areas = Arrays.stream(m.getAreasAtendidas().split(","))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .toList();
        return new MentorDto(m.getId(), m.getNome(), m.getPapel(), areas, m.getLinkSala());
    }
}
