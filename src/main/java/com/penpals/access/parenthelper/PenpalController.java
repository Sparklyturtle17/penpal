package com.penpals.access.parenthelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/penpal/penpals")
@PreAuthorize("hasAnyRole('PENPAL')")
@Slf4j
@RequiredArgsConstructor
public class PenpalController {

}
